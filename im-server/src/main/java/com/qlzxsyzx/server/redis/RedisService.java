package com.qlzxsyzx.server.redis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@Component
public class RedisService {
    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    public void setUserChannel(Long userId, String platform, String channelId) {
        redisTemplate.opsForValue().set("ws:channel:" + userId + ":" + platform, channelId);
    }

    public List<String> getChannelIdFromRedis(Long userId) {
        // 从redis取出key like ws:channel:{userId}:*
        return keysWithScan("ws:channel:" + userId + ":*");
    }

    public String getChannelIdFromRedis(Long userId, String platform) {
        return redisTemplate.opsForValue()
                .get("ws:channel:" + userId + ":" + platform);
    }

    // 阻塞
    private Set<String> keysWithPattern(String pattern) {
        return redisTemplate.keys(pattern);
    }

    // 非阻塞
    private List<String> keysWithScan(String pattern) {
        ScanOptions options = ScanOptions.scanOptions().match(pattern).build();
        List<String> keys = new ArrayList<>();
        Cursor<byte[]> cursor = redisTemplate.getConnectionFactory().getConnection().scan(options);
        while (cursor.hasNext()) {
            keys.add(new String(cursor.next()));
        }
        return redisTemplate.opsForValue().multiGet(keys);
    }

    public void casDeleteKey(String key, String expectValue) {
        String script =
                "if redis.call('get', KEYS[1]) == ARGV[1] then " +
                        "return redis.call('del', KEYS[1]) " +
                        "else " +
                        "return 0 " +
                        "end";
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>(script, Long.class);
        redisTemplate.execute(redisScript, Collections.singletonList(key), expectValue);
    }
}
