package com.qlzxsyzx.web.controller;

import com.qlzxsyzx.common.ResponseEntity;
import com.qlzxsyzx.resource.annotation.AuthenticationDetails;
import com.qlzxsyzx.web.dto.CreateMessageDto;
import com.qlzxsyzx.web.dto.GetChatMessageListDto;
import com.qlzxsyzx.web.dto.GetGroupChatMessageHistoryDto;
import com.qlzxsyzx.web.dto.GetSingleChatMessageHistoryDto;
import com.qlzxsyzx.web.service.ChatService;
import com.qlzxsyzx.web.service.RecentChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/chat")
public class ChatController {
    @Autowired
    private ChatService chatService;

    @GetMapping("/recentChatList")
    public ResponseEntity getRecentChatList(@AuthenticationDetails("userId") Long userId) {
        return chatService
                .getRecentChatList(userId);
    }

    @PostMapping("/createSingleChat/{friendId}")
    public ResponseEntity createSingleChat(@AuthenticationDetails("userId") Long userId, @PathVariable("friendId") Long friendId) {
        return chatService.createSingleChat(userId, friendId);
    }

    @PostMapping("/createGroupChat/{groupId}")
    public ResponseEntity createGroupChat(@AuthenticationDetails("userId") Long userId, @PathVariable("groupId") Long groupId) {
        return chatService.createGroupChat(userId, groupId);
    }

    @PostMapping("/sendMessage")
    public ResponseEntity sendMessage(@AuthenticationDetails("userId") Long userId, @RequestBody CreateMessageDto createMessageDto) {
        return chatService.sendMessage(userId, createMessageDto);
    }

    @PostMapping("/getChatMessageList")
    public ResponseEntity getChatMessageList(@AuthenticationDetails("userId") Long userId, @RequestBody GetChatMessageListDto getChatMessageListDto) {
        return chatService.getChatMessageList(userId, getChatMessageListDto);
    }

    @PostMapping("/topChat/{id}/{status}")
    public ResponseEntity topChat(@AuthenticationDetails("userId") Long userId, @PathVariable("id") Long id, @PathVariable("status") Integer status) {
        return chatService.topChat(userId, id, status);
    }

    @PostMapping("/getSingleChatMessageHistory")
    public ResponseEntity getSingleChatMessageHistory(@AuthenticationDetails("userId") Long userId, @RequestBody GetSingleChatMessageHistoryDto getSingleChatMessageHistoryDto) {
        return chatService.getSingleChatMessageHistory(userId, getSingleChatMessageHistoryDto);
    }

    @PostMapping("/getGroupChatMessageHistory")
    public ResponseEntity getGroupChatMessageHistory(@AuthenticationDetails("userId") Long userId, @RequestBody GetGroupChatMessageHistoryDto getGroupChatMessageHistoryDto) {
        return chatService.getGroupChatMessageHistory(userId, getGroupChatMessageHistoryDto);
    }
}
