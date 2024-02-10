package com.qlzxsyzx.web.controller;

import com.qlzxsyzx.common.ResponseEntity;
import com.qlzxsyzx.resource.annotation.AuthenticationDetails;
import com.qlzxsyzx.web.dto.*;
import com.qlzxsyzx.web.service.GroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/group")
public class GroupController {
    @Autowired
    private GroupService groupService;

    @GetMapping("/getGroupList")
    public ResponseEntity getGroupList(@AuthenticationDetails("userId") Long userId) {
        return groupService.getGroupList(userId);
    }

    @PostMapping("/createGroup")
    public ResponseEntity createGroup(@AuthenticationDetails("userId") Long userId, @RequestBody CreateGroupDto createGroupDto) {
        return groupService.createGroup(userId, createGroupDto);
    }

    @GetMapping("/getGroupInfo/{groupId}")
    public ResponseEntity getGroupInfo(@AuthenticationDetails("userId") Long userId, @PathVariable("groupId") Long groupId) {
        return groupService.getGroupInfo(userId, groupId);
    }

    @PostMapping("/updateGroupRemark")
    public ResponseEntity updateGroupRemark(@AuthenticationDetails("userId") Long userId, @RequestBody UpdateGroupRemarkDto updateGroupRemarkDto){
        return groupService.updateGroupRemark(userId, updateGroupRemarkDto);
    }

    @PostMapping("/updateUserNickName")
    public ResponseEntity updateUserNickName(@AuthenticationDetails("userId") Long userId, @RequestBody UpdateUserNickNameDto updateUserNickNameDto){
        return groupService.updateUserNickName(userId, updateUserNickNameDto);
    }

    @PostMapping("/updateGroupPromptStatus/{id}/{status}")
    public ResponseEntity updateGroupPromptStatus(@AuthenticationDetails("userId") Long userId, @PathVariable("id") Long id, @PathVariable("status") Integer status){
        return groupService.updateGroupPromptStatus(userId, id, status);
    }

    @PostMapping("/updateGroupName")
    public ResponseEntity updateGroupName(@AuthenticationDetails("userId") Long userId, @RequestBody UpdateGroupNameDto updateGroupNameDto){
        return groupService.updateGroupName(userId, updateGroupNameDto);
    }

    @PostMapping("/updateGroupAvatar")
    public ResponseEntity updateGroupAvatar(@AuthenticationDetails("userId") Long userId, @RequestBody UpdateGroupAvatarDto updateGroupAvatarDto){
        return groupService.updateGroupAvatar(userId, updateGroupAvatarDto);
    }
}
