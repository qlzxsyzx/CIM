package com.qlzxsyzx.web.mq;

import com.qlzxsyzx.common.mq.ChatMessage;
import com.qlzxsyzx.common.mq.FileDetails;
import com.qlzxsyzx.mq.client.RabbitMQClient;
import com.qlzxsyzx.web.entity.GroupMember;
import com.qlzxsyzx.web.feign.FileFeignClient;
import com.qlzxsyzx.web.service.GroupMemberService;
import com.qlzxsyzx.web.vo.ChatMessageVo;
import com.qlzxsyzx.web.vo.FileDetailsVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MQSendService {
    @Autowired
    private RabbitMQClient rabbitMQClient;

    @Autowired
    private FileFeignClient fileFeignClient;

    @Autowired
    private GroupMemberService groupMemberService;

    @Async("sendMessageTaskExecutor")
    public void asyncSendMessage(ChatMessage chatMessage) {
        // 发送消息到队列
        rabbitMQClient.sendChatMessage(chatMessage);
    }

    @Async("sendMessageTaskExecutor")
    public void asyncSendMessageVo(ChatMessageVo chatMessage) {
        // 在这里解析消息
        Long recordId = chatMessage.getRecordId();
        ChatMessage message = new ChatMessage();
        BeanUtils.copyProperties(chatMessage, message);
        if (recordId != null) {
            FileDetailsVo fileDetails = fileFeignClient.getFileDetails(recordId);
            FileDetails fileInfo = new FileDetails();
            BeanUtils.copyProperties(fileDetails, fileInfo);
            message.setFileInfo(fileInfo);
        }
        Integer receiverType = chatMessage.getReceiverType();
        if (receiverType == 0) {
            // 单聊，直接发送
            rabbitMQClient.sendChatMessage(message);
        } else {
            // 群聊，先查询群成员，再逐个发送
            Long groupId = chatMessage.getReceiverId();
            List<GroupMember> members = groupMemberService.query().eq("group_id", groupId)
                    .eq("exit_type", 0)
                    .ne("user_id", chatMessage.getSenderId()).list();
            for (GroupMember member : members) {
                message.setReceiverId(member.getUserId());
                rabbitMQClient.sendChatMessage(message);
            }
        }
    }

    @Async("sendMessageTaskExecutor")
    public void asyncSendSingleSystemMessage(Integer type,Long roomId,Long toUserId, String content) {

    }

    @Async("sendMessageTaskExecutor")
    public void asyncSendGroupSystemMessage(Integer type,Long roomId,Long groupId, String content) {

    }
}
