package com.qlzxsyzx.snowflake.utils;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

/**
 * 分布式全局ID雪花算法解决方案
 * 防止时钟回拨
 * 因为机器的原因会发生时间回拨，我们的雪花算法是强依赖我们的时间的，如果时间发生回拨，有可能会生成重复的ID，在我们上面的nextId中我们用当前时间和上一次的时间进行判断，
 * 如果当前时间小于上一次的时间那么肯定是发生了回拨，普通的算法会直接抛出异常,这里我们可以对其进行优化,一般分为两个情况:
 * 1如果时间回拨时间较短，比如配置5ms以内，那么可以直接等待一定的时间，让机器的时间追上来。
 * 2如果时间的回拨时间较长，我们不能接受这么长的阻塞等待，那么又有两个策略:
 * 1直接拒绝，抛出异常，打日志，通知RD时钟回滚。
 * 2利用扩展位，上面我们讨论过不同业务场景位数可能用不到那么多，那么我们可以把扩展位数利用起来了，
 * 比如当这个时间回拨比较长的时候，我们可以不需要等待，直接在扩展位加1。
 * 2位的扩展位允许我们有3次大的时钟回拨，一般来说就够了，如果其超过三次我们还是选择抛出异常，打日志。
 * 通过上面的几种策略可以比较的防护我们的时钟回拨，防止出现回拨之后大量的异常出现。下面是修改之后的代码，这里修改了时钟回拨的逻辑:
 */
@Slf4j
public class SnowflakeUtil {
    /**
     * Epoch，这个是起始时间，设置后不允许修改
     */
    private static final long EPOCH = 1704201341427L;

    /**
     * 每台workerId服务器有3个备份workerId, 备份workerId数量越多, 可靠性越高, 但是可部署的sequence ID服务越少
     */
    private static final long BACKUP_COUNT = 3;

    /**
     * 机器标识位数
     */
    private static final long WORKER_ID_BITS = 5L;

    /**
     * 数据中心标识位数
     */
    private static final long DATA_CENTER_ID_BITS = 5L;

    /**
     * 业务标识位数
     */
    private static final long SEQUENCE_BITS = 12L;

    /**
     * 机器ID左移12位
     */
    private static final long WORKER_ID_SHIFT = SEQUENCE_BITS;

    /**
     * 数据中心ID左移17位
     */
    private static final long DATA_CENTER_ID_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS;

    /**
     * 时间毫秒左移22位
     */
    private static final long TIMESTAMP_LEFT_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS +
            DATA_CENTER_ID_BITS;

    /**
     * sequence掩码，确保sequence不会超出sequence空间,最大的序列号 4096
     */
    private static final long SEQUENCE_MASK = ~(-1L << SEQUENCE_BITS);

    /**
     * 工作机器ID(0~31)，但需要为每台机器预留BackupCount个备份机器ID
     */
    private static final long MAX_WORKER_ID = ~(-1L << WORKER_ID_BITS);

    /**
     * 数据中心ID(0~31)
     */
    private static final long MAX_DATA_CENTER_ID = ~(-1L << DATA_CENTER_ID_BITS);

    /**
     * 工作机器ID(0~31)
     * snowflake算法给workerId预留了10位，即workId的取值范围为[0, 1023]，
     * 事实上实际生产环境不大可能需要部署1024个分布式ID服务，
     * 所以：将workerId取值范围缩小为[0, 255]
     */
    private static long workerId;

    private static long dataCenterId;

    private long sequence = 0L;

    private long lastTimestamp = -1L;

    /**
     * 最大容忍时间, 单位毫秒, 即如果时钟只是回拨了该变量指定的时间, 那么等待相应的时间即可;
     * 考虑到sequence服务的高性能, 这个值不易过大
     */
    private static final long MAX_BACKWARD_MS = 3;

    public SnowflakeUtil(long workerId, long dataCenterId) {
        if (workerId > MAX_WORKER_ID || workerId < 0) {
            throw new IllegalArgumentException(String.format("worker Id can't be greater than %d or less than 0", MAX_WORKER_ID));
        }
        if (dataCenterId > MAX_DATA_CENTER_ID || dataCenterId < 0) {
            throw new IllegalArgumentException(String.format("datacenter Id can't be greater than %d or less than 0", MAX_DATA_CENTER_ID));
        }
        this.workerId = workerId;
        this.dataCenterId = dataCenterId;
    }

    /**
     * 获取指定数量的下一个ID
     *
     * @param count count
     * @return long[]
     */
    public synchronized Long[] nextIdBatch(int count) {
        if (count <= 0) {
            throw new IllegalArgumentException("Count must be greater than 0");
        }
        if (count > 1024) {
            throw new IllegalArgumentException("Count can't be greater than 1024");
        }
        Long[] ids = new Long[count];
        for (int i = 0; i < count; i++) {
            ids[i] = nextId();
        }
        return ids;
    }

    /**
     * 获得下一个ID (该方法是线程安全的)
     * 在单节点上获得下一个ID，使用Synchronized控制并发，而非CAS的方式，
     * 是因为CAS不适合并发量非常高的场景。
     * <p>
     * 考虑时钟回拨
     * 缺陷: 如果连续两次时钟回拨, 可能还是会有问题, 但是这种概率极低极低
     *
     * @return
     */
    public synchronized long nextId() {
        long timestamp = timeGen();

        // 如果当前时间小于上一次ID生成的时间戳, 说明系统时钟回退过这个时候应当抛出异常
        if (timestamp < lastTimestamp) {
            long offset = lastTimestamp - timestamp;
            // 时钟回拨在可接受范围，等待
            if (offset <= MAX_BACKWARD_MS) {
                // 等待时钟恢复
                LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(offset));
                timestamp = timeGen();
                // 再次检查时间戳
                if (timestamp < lastTimestamp) {
                    throw new RuntimeException(String.format("Clock moved backwards. Refusing to generate id for %d milliseconds", offset));
                }
            } else {
                // throw new RuntimeException(String.format("Clock moved backwards. Refusing to generate id for %d milliseconds", offset));
                // 使用备用worker生成ID
                tryGenerateKeyOnBackup();
            }
        }

        // 如果是同一时间生成的，则进行毫秒内序列
        if (lastTimestamp == timestamp) {
            sequence = (sequence + 1) & SEQUENCE_MASK;
            // 毫秒内序列溢出
            if (sequence == 0) {
                // 阻塞到下一个毫秒,获得新的时间戳
                timestamp = tilNextMillis(lastTimestamp);
            }
        }
        // 时间戳改变，毫秒内序列重置
        else {
            sequence = 0L;
        }

        // 上次生成ID的时间截
        lastTimestamp = timestamp;

        // 移位并通过或运算拼到一起组成64位的ID
        return ((timestamp - EPOCH) << TIMESTAMP_LEFT_SHIFT) |
                (dataCenterId << DATA_CENTER_ID_SHIFT) |
                (workerId << WORKER_ID_SHIFT) |
                sequence;
    }

    /**
     * 尝试在workerId的备份workerId上生成
     * 核心优化代码在方法tryGenerateKeyOnBackup()中，BACKUP_COUNT即备份workerId数越多，
     * sequence服务避免时钟回拨影响的能力越强，但是可部署的sequence服务越少，
     * 设置BACKUP_COUNT为3，最多可以部署1024/(3+1)即256个sequence服务，完全够用，
     * 抗时钟回拨影响的能力也得到非常大的保障。
     */
    private void tryGenerateKeyOnBackup() {
        if (workerId >= BACKUP_COUNT * 256) {
            throw new RuntimeException("Backup workerId has been used up");
        }
        workerId = workerId + 256;
        lastTimestamp = timeGen();
    }

    /**
     * 阻塞到下一个毫秒，直到获得新的时间戳
     *
     * @param lastTimestamp 上次生成ID的时间截
     */
    private long tilNextMillis(long lastTimestamp) {
        long timestamp = timeGen();
        while (timestamp <= lastTimestamp) {
            timestamp = timeGen();
        }
        return timestamp;
    }

    private long timeGen() {
        return System.currentTimeMillis();
    }
}
