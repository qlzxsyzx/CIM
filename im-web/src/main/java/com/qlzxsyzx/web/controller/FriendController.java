package com.qlzxsyzx.web.controller;

import com.qlzxsyzx.common.ResponseEntity;
import com.qlzxsyzx.resource.annotation.AuthenticationDetails;
import com.qlzxsyzx.web.dto.AgreeApplyFriendMessageDto;
import com.qlzxsyzx.web.dto.ApplyFriendDto;
import com.qlzxsyzx.web.service.BlackListService;
import com.qlzxsyzx.web.service.FriendService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/friend")
public class FriendController {
    @Autowired
    private FriendService friendService;

    @Autowired
    private BlackListService blackListService;

    // 搜索用户
    @GetMapping("/searchByUsername/{username}")
    public ResponseEntity searchByUsername(@PathVariable("username") String username) {
        return friendService.searchByUsername(username);
    }

    @PostMapping("/applyAddFriend")
    public ResponseEntity applyAddFriend(@AuthenticationDetails("userId") Long userId, @RequestBody ApplyFriendDto applyFriendDto) {
        return friendService.applyAddFriend(userId, applyFriendDto);
    }

    @PostMapping("/agreeAddFriend")
    public ResponseEntity agreeAddFriend(@AuthenticationDetails("userId") Long userId, @RequestBody AgreeApplyFriendMessageDto agreeApplyFriendMessageDto) {
        Long applyFriendMessageId = agreeApplyFriendMessageDto.getApplyFriendMessageId();
        String remark = agreeApplyFriendMessageDto.getRemark();
        return friendService.agreeAddFriend(userId, applyFriendMessageId, remark);
    }

    @PostMapping("/refuseAddFriend/{applyFriendMessageId}")
    public ResponseEntity refuseAddFriend(@AuthenticationDetails("userId") Long userId, @PathVariable("applyFriendMessageId") Long applyFriendMessageId) {
        return friendService.refuseAddFriend(userId, applyFriendMessageId);
    }

    @GetMapping("/getApplyFriendMessageList")
    public ResponseEntity getApplyFriendMessageList(@AuthenticationDetails("userId") Long userId) {
        return friendService.getApplyFriendMessageList(userId);
    }

    @PostMapping("/blockFriend/{friendId}")
    public ResponseEntity blockFriend(@AuthenticationDetails("userId") Long userId, @PathVariable("friendId") Long friendId) {
        return blackListService.blockFriend(userId, friendId);
    }

    @PostMapping("/removeBlackList/{friendId}")
    public ResponseEntity removeBlackList(@AuthenticationDetails("userId") Long userId, @PathVariable("friendId") Long friendId) {
        return blackListService.removeBlackList(userId, friendId);
    }

    @GetMapping("/getFriendList")
    public ResponseEntity getFriendList(@AuthenticationDetails("userId") Long userId) {
        return friendService.getFriendList(userId);
    }
}
