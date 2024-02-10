package com.qlzxsyzx.web.controller;

import com.qlzxsyzx.common.ResponseEntity;
import com.qlzxsyzx.resource.annotation.AuthenticationDetails;
import com.qlzxsyzx.web.dto.AgreeApplyFriendMessageDto;
import com.qlzxsyzx.web.dto.ApplyFriendDto;
import com.qlzxsyzx.web.dto.UpdateRemarkDto;
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

    @GetMapping("/getBlackList")
    public ResponseEntity getBlackList(@AuthenticationDetails("userId") Long userId) {
        return friendService.getBlackList(userId);
    }

    @PostMapping("/blockUser/{toUserId}")
    public ResponseEntity blockUser(@AuthenticationDetails("userId") Long userId, @PathVariable("toUserId") Long toUserId) {
        return friendService.blockUser(userId, toUserId);
    }

    @PostMapping("/removeBlackList/{toUserId}")
    public ResponseEntity removeBlackList(@AuthenticationDetails("userId") Long userId, @PathVariable("toUserId") Long toUserId) {
        return friendService.removeBlackList(userId, toUserId);
    }

    @PostMapping("/removeFriend/{friendId}")
    public ResponseEntity removeFriend(@AuthenticationDetails("userId") Long userId, @PathVariable("friendId") Long friendId) {
        return friendService.removeFriend(userId, friendId);
    }

    @PostMapping("/updatePromptStatus/{id}/{status}")
    public ResponseEntity updatePromptStatus(@AuthenticationDetails("userId") Long userId, @PathVariable("id") Long id, @PathVariable("status") Integer status) {
        return friendService.updatePromptStatus(userId, id, status);
    }

    @GetMapping("/getFriendList")
    public ResponseEntity getFriendList(@AuthenticationDetails("userId") Long userId) {
        return friendService.getFriendList(userId);
    }

    @PostMapping("/updateRemark")
    public ResponseEntity updateRemark(@AuthenticationDetails("userId") Long userId, @RequestBody UpdateRemarkDto updateRemarkDto) {
        return friendService.updateRemark(userId, updateRemarkDto);
    }
}
