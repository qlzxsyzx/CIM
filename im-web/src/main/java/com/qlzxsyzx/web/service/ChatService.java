package com.qlzxsyzx.web.service;

import com.qlzxsyzx.common.ResponseEntity;
import com.qlzxsyzx.web.dto.CreateMessageDto;
import com.qlzxsyzx.web.dto.GetChatMessageListDto;
import com.qlzxsyzx.web.dto.GetGroupChatMessageHistoryDto;
import com.qlzxsyzx.web.dto.GetSingleChatMessageHistoryDto;

public interface ChatService {
    ResponseEntity getRecentChatList(Long userId);

    ResponseEntity sendMessage(Long userId, CreateMessageDto createMessageDto);

    ResponseEntity getChatMessageList(Long userId, GetChatMessageListDto getChatMessageListDto);

    ResponseEntity createSingleChat(Long userId, Long friendId);

    ResponseEntity createGroupChat(Long userId, Long groupId);

    ResponseEntity topChat(Long userId, Long id, Integer status);

    ResponseEntity getSingleChatMessageHistory(Long userId, GetSingleChatMessageHistoryDto getSingleChatMessageHistoryDto);

    ResponseEntity getGroupChatMessageHistory(Long userId, GetGroupChatMessageHistoryDto getGroupChatMessageHistoryDto);
}
