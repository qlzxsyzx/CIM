package com.qlzxsyzx.web.service;

import com.qlzxsyzx.common.ResponseEntity;
import com.qlzxsyzx.web.dto.CreateMessageDto;

public interface ChatService {
    ResponseEntity getRecentChatList(Long userId);

    ResponseEntity sendMessage(Long userId, CreateMessageDto createMessageDto);

    ResponseEntity getChatMessageList(Long userId, Long roomId, Integer pageNum, Integer pageSize);

    ResponseEntity createSingleChat(Long userId, Long friendId);

    ResponseEntity createGroupChat(Long userId, Long groupId);

    ResponseEntity topChat(Long userId, Long id, Integer status);
}
