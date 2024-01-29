package com.qlzxsyzx.server.netty.channel;

import com.qlzxsyzx.server.redis.RedisService;
import io.netty.channel.Channel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ChannelMapService {
    private final Map<String, Channel> CHANNEL_MAP = new ConcurrentHashMap<>();

    private final Map<String, String> CHANNEL_ID_REDIS_KEY_MAP = new ConcurrentHashMap<>();

    @Autowired
    private RedisService redisService;

    // 添加通道
    public void addChannel(Long userId, String platform, Channel channel) {
        CHANNEL_MAP.put(channel.id().asLongText(), channel);
        CHANNEL_ID_REDIS_KEY_MAP.put(channel.id().asLongText(), "ws:channel:" + userId + ":" + platform);
    }

    // 移除通道
    public void removeChannel(Channel channel) {
        String longText = channel.id().asLongText();
        CHANNEL_MAP.remove(longText);
        String wsRedisKey = CHANNEL_ID_REDIS_KEY_MAP.get(longText);
        redisService.casDeleteKey(wsRedisKey, longText);
        CHANNEL_ID_REDIS_KEY_MAP.remove(longText);
    }

    // 根据通道ID获取通道
    public Channel getChannel(String channelId) {
        return CHANNEL_MAP.get(channelId);
    }
}
