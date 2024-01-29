package com.qlzxsyzx.web.mq;

import com.qlzxsyzx.common.mq.ChatMessage;
import com.qlzxsyzx.mq.client.RabbitMQClient;
import com.qlzxsyzx.web.vo.ChatMessageVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class MQSendService {
    @Autowired
    private RabbitMQClient rabbitMQClient;

    @Async("sendMessageTaskExecutor")
    public void asyncSendMessage(ChatMessage chatMessage) {
        // 发送消息到队列
        rabbitMQClient.sendChatMessage(chatMessage);
    }

    @Async("sendMessageTaskExecutor")
    public void asyncSendMessageVo(ChatMessageVo chatMessage) {
        ChatMessage message = new ChatMessage();
        BeanUtils.copyProperties(chatMessage, message);
        // 发送消息到队列
        rabbitMQClient.sendChatMessage(message);
    }
}
