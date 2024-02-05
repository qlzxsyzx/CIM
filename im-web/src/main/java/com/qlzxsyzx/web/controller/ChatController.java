package com.qlzxsyzx.web.controller;

import com.qlzxsyzx.common.ResponseEntity;
import com.qlzxsyzx.resource.annotation.AuthenticationDetails;
import com.qlzxsyzx.web.dto.CreateMessageDto;
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

    @GetMapping("/getChatMessageList/{roomId}/{pageNum}/{pageSize}")
    public ResponseEntity getChatMessageList(@AuthenticationDetails("userId") Long userId, @PathVariable("roomId") Long roomId,
                                             @PathVariable("pageNum") Integer pageNum, @PathVariable("pageSize") Integer pageSize) {
        return chatService.getChatMessageList(userId, roomId, pageNum, pageSize);
    }

    @PostMapping("/topChat/{id}/{status}")
    public ResponseEntity topChat(@AuthenticationDetails("userId") Long userId, @PathVariable("id") Long id, @PathVariable("status") Integer status) {
        return chatService.topChat(userId, id, status);
    }
}
