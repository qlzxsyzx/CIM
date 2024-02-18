package com.qlzxsyzx.server.netty.publisher;

import com.qlzxsyzx.common.mq.ChatMessage;
import com.qlzxsyzx.common.mq.FileDetails;
import com.qlzxsyzx.common.mq.SystemNotification;
import com.qlzxsyzx.common.mq.SystemNotificationForChannel;
import com.qlzxsyzx.mq.client.RabbitMQClient;
import com.qlzxsyzx.server.netty.channel.ChannelMapService;
import com.qlzxsyzx.server.netty.proto.CustomMessage;
import com.qlzxsyzx.server.redis.RedisService;
import io.netty.channel.Channel;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Component
public class NettyPublisher {
    @Autowired
    private RabbitMQClient rabbitMQClient;

    @Autowired
    private RedisService redisService;

    @Autowired
    private ChannelMapService channelMapService;

    public void noticeUserAllWs(Long userId, SystemNotification systemNotification) {
        // 从redis判断ws是否存在
        List<String> channelIdList = redisService.getChannelIdFromRedis(userId);
        if (CollectionUtils.isEmpty(channelIdList)) {
            // 不存在，不发送消息
            return;
        }
        // 发送消息
        for (String channelId : channelIdList) {
            noticeUserWs(channelId, systemNotification);
        }
    }

    public void noticeUserWs(String channelId, SystemNotification systemNotification) {
        if (StringUtils.isBlank(channelId)) {
            return;
        }
        // 查询是否是本地Channel
        Channel channel = channelMapService.getChannel(channelId);
        if (channel == null) {
            return;
        }
        // 本地Channel，直接发送消息
        if (channel.isActive()) {
            CustomMessage.Message.Builder builder = CustomMessage.Message.newBuilder();
            builder.setHeadType(CustomMessage.HeadType.SYSTEM_NOTIFICATION.getNumber());
            builder.setSystemNotification(getProtoSystemNotification(systemNotification));
            channel.writeAndFlush(builder.build());
        }
    }


    // 本地发送ws消息,由netty自己发送给用户
    public void localNoticeUserWs(Channel channel, Long userId, String platform, SystemNotification systemNotification) {
        // 从redis判断ws是否存在
        String channelIdFromRedis = redisService.getChannelIdFromRedis(userId, platform);
        if (StringUtils.isBlank(channelIdFromRedis)) {
            // 不存在，不发送消息
            return;
        }
        // 存在
        // 查询是否是本地Channel
        Channel localChannel = channelMapService.getChannel(channelIdFromRedis);
        if (localChannel == null) {
            // MQ广播
            rabbitMQClient.sendSystemMessage(new SystemNotificationForChannel(systemNotification, channelIdFromRedis));
            return;
        }
        if (channel.equals(localChannel)) {
            // 同一channel,不发送消息
            return;
        }
        // 本地Channel，直接发送消息
        if (localChannel.isActive()) {
            CustomMessage.Message.Builder builder = CustomMessage.Message.newBuilder();
            builder.setHeadType(CustomMessage.HeadType.SYSTEM_NOTIFICATION.getNumber());
            builder.setSystemNotification(getProtoSystemNotification(systemNotification));
            channel.writeAndFlush(builder.build());
        }
    }

    public void messageUserAllWs(Long userId, ChatMessage chatMessage) {
        // 从redis判断ws是否存在
        List<String> channelIdList = redisService.getChannelIdFromRedis(userId);
        if (CollectionUtils.isEmpty(channelIdList)) {
            // 不存在，不发送消息
            return;
        }
        // 存在
        for (String channelIdFromRedis : channelIdList) {
            messageUserWs(channelIdFromRedis, chatMessage);
        }
    }

    public void messageUserWs(String channelId, ChatMessage chatMessage) {
        if (StringUtils.isBlank(channelId)) {
            return;
        }
        // 查询是否是本地Channel
        Channel channel = channelMapService.getChannel(channelId);
        if (channel == null) {
            return;
        }
        // 本地Channel，直接发送消息
        if (channel.isActive()) {
            CustomMessage.Message.Builder builder = CustomMessage.Message.newBuilder();
            builder.setHeadType(CustomMessage.HeadType.MESSAGE_REQUEST.getNumber());
            builder.setMessageRequest(getProtoMessageRequest(chatMessage));
            channel.writeAndFlush(builder.build());
        }
    }

    private CustomMessage.MessageRequest getProtoMessageRequest(ChatMessage chatMessage) {
        CustomMessage.MessageRequest.Builder builder = CustomMessage.MessageRequest.newBuilder();
        builder.setMessageId(chatMessage.getMessageId());
        builder.setRoomId(chatMessage.getRoomId());
        builder.setSenderId(chatMessage.getSenderId());
        builder.setReceiverId(chatMessage.getReceiverId());
        builder.setType(chatMessage.getType());
        builder.setContent(chatMessage.getContent());
        FileDetails fileInfo = chatMessage.getFileInfo();
        if (fileInfo != null){
            CustomMessage.FileInfo.Builder fileInfoBuilder = CustomMessage.FileInfo.newBuilder();
            fileInfoBuilder.setRecordId(fileInfo.getRecordId());
            fileInfoBuilder.setRealName(fileInfo.getRealName());
            fileInfoBuilder.setExt(fileInfo.getExt());
            fileInfoBuilder.setFileSize(fileInfo.getFileSize());
            builder.setFileInfo(fileInfoBuilder.build());
        }
        builder.setCreateTime(chatMessage.getCreateTime().toString());
        return builder.build();
    }

    private CustomMessage.SystemNotification getProtoSystemNotification(SystemNotification systemNotification) {
        CustomMessage.SystemNotification.Builder builder = CustomMessage.SystemNotification.newBuilder();
        builder.setType(systemNotification.getType());
        builder.setCode(systemNotification.getCode());
        builder.setUserId(systemNotification.getUserId());
        builder.setContent(systemNotification.getContent());
        builder.setCreateTime(systemNotification.getCreateTime().toString());
        return builder.build();
    }
}
