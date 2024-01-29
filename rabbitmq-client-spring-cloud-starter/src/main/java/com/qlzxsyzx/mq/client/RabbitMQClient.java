package com.qlzxsyzx.mq.client;

import com.qlzxsyzx.common.mq.ChatMessage;
import com.qlzxsyzx.common.mq.SystemNotification;
import com.qlzxsyzx.common.mq.SystemNotificationForChannel;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

public class RabbitMQClient {
    private static final String CHAT_EXCHANGE_NAME = "CIM-NETTY";

    private static final String SYSTEM_EXCHANGE_NAME = "CIM-SYSTEM";

    private final RabbitTemplate rabbitTemplate;

    public RabbitMQClient(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendChatMessage(ChatMessage message) {
        rabbitTemplate.convertAndSend(CHAT_EXCHANGE_NAME, "", message);
    }

    public void sendSystemMessage(SystemNotification message) {
        rabbitTemplate.convertAndSend(SYSTEM_EXCHANGE_NAME, "", message);
    }

    public void sendSystemMessage(SystemNotificationForChannel message) {
        rabbitTemplate.convertAndSend(SYSTEM_EXCHANGE_NAME, "", message);
    }
}
