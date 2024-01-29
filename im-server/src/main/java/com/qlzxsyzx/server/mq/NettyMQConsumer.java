package com.qlzxsyzx.server.mq;

import com.qlzxsyzx.common.mq.ChatMessage;
import com.qlzxsyzx.common.mq.SystemNotification;
import com.qlzxsyzx.common.mq.SystemNotificationForChannel;
import com.qlzxsyzx.common.type.NotificationType;
import com.qlzxsyzx.server.netty.proto.CustomMessage;
import com.qlzxsyzx.server.netty.publisher.NettyPublisher;
import io.netty.channel.Channel;
import org.apache.commons.lang.StringUtils;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class NettyMQConsumer {
    private static final String CHAT_EXCHANGE_NAME = "CIM-NETTY";

    private static final String SYSTEM_EXCHANGE_NAME = "CIM-SYSTEM";

    @Autowired
    private NettyPublisher nettyPublisher;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue,
            exchange = @Exchange(value = CHAT_EXCHANGE_NAME, type = ExchangeTypes.FANOUT)
    ))
    public void receiveChatMessage(ChatMessage message) {
        // 处理消息
        Long receiverId = message.getReceiverId();
        // 将消息发送给指定channelId的客户端
        if (receiverId != null) {
            nettyPublisher.messageUserAllWs(receiverId, message);
        }
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue,
            exchange = @Exchange(value = SYSTEM_EXCHANGE_NAME, type = ExchangeTypes.FANOUT)
    ))
    public void receiveSystemMessage(SystemNotification systemNotification) {
        // 处理系统通知消息
        Long userId = systemNotification.getUserId();
        if (userId != null) {
            nettyPublisher.noticeUserAllWs(userId, systemNotification);
        }
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue,
            exchange = @Exchange(value = SYSTEM_EXCHANGE_NAME, type = ExchangeTypes.FANOUT)
    ))
    public void receiveSystemMessage(SystemNotificationForChannel systemNotificationForChannel) {
        // 处理固定channelId的系统通知消息
        String channelId = systemNotificationForChannel.getChannelId();
        nettyPublisher.noticeUserWs(channelId, systemNotificationForChannel.getSystemNotification());
    }
}
