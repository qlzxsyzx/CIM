package com.qlzxsyzx.web.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qlzxsyzx.web.entity.ChatMessage;
import com.qlzxsyzx.web.mapper.ChatMessageMapper;
import com.qlzxsyzx.web.service.ChatMessageService;
import com.qlzxsyzx.web.vo.ChatMessageVo;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ChatMessageServiceImpl extends ServiceImpl<ChatMessageMapper, ChatMessage> implements ChatMessageService {
    @Override
    public ChatMessage getLatestMessageByRoomId(Long roomId) {
        return query().eq("room_id", roomId)
                .eq("status", 1)
                .orderByDesc("create_time").last("limit 1").one();
    }

    @Override
    public Map<Long, ChatMessageVo> getRoomIdAndLatestMessageMap(List<Long> roomIds) {
        List<ChatMessage> chatMessages = baseMapper.listLatestMessageGroupByRoomId(roomIds);
        if (chatMessages == null || chatMessages.isEmpty()){
            return Collections.emptyMap();
        }
        return chatMessages.stream().collect(Collectors.toMap(ChatMessage::getRoomId, this::convertToChatMessageVo));
    }

    private ChatMessageVo convertToChatMessageVo(ChatMessage chatMessage) {
        // 转换为ChatMessageVo对象
        ChatMessageVo chatMessageVo = new ChatMessageVo();
        BeanUtils.copyProperties(chatMessage, chatMessageVo);
        return chatMessageVo;
    }

    private List<ChatMessageVo> convertToChatMessageVoList(List<ChatMessage> chatMessages) {
        // 转换为ChatMessageVo列表
        return chatMessages.stream().map(this::convertToChatMessageVo).collect(Collectors.toList());
    }
}
