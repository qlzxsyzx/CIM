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
    public ResponseEntity updateGroupRemark(@AuthenticationDetails("userId") Long userId, @RequestBody UpdateGroupRemarkDto updateGroupRemarkDto) {
        return groupService.updateGroupRemark(userId, updateGroupRemarkDto);
    }

    @PostMapping("/updateUserNickName")
    public ResponseEntity updateUserNickName(@AuthenticationDetails("userId") Long userId, @RequestBody UpdateUserNickNameDto updateUserNickNameDto) {
        return groupService.updateUserNickName(userId, updateUserNickNameDto);
    }

    @PostMapping("/updateGroupPromptStatus/{id}/{status}")
    public ResponseEntity updateGroupPromptStatus(@AuthenticationDetails("userId") Long userId, @PathVariable("id") Long id, @PathVariable("status") Integer status) {
        return groupService.updateGroupPromptStatus(userId, id, status);
    }

    @PostMapping("/updateGroupName")
    public ResponseEntity updateGroupName(@AuthenticationDetails("userId") Long userId, @RequestBody UpdateGroupNameDto updateGroupNameDto) {
        return groupService.updateGroupName(userId, updateGroupNameDto);
    }

    @PostMapping("/updateGroupAvatar")
    public ResponseEntity updateGroupAvatar(@AuthenticationDetails("userId") Long userId, @RequestBody UpdateGroupAvatarDto updateGroupAvatarDto) {
        return groupService.updateGroupAvatar(userId, updateGroupAvatarDto);
    }

    @GetMapping("/getGroupMemberList/{id}/{pageNum}/{pageSize}")
    public ResponseEntity getGroupMemberList(@AuthenticationDetails("userId") Long userId, @PathVariable("id") Long id, @PathVariable("pageNum") Integer pageNum, @PathVariable("pageSize") Integer pageSize) {
        return groupService.getGroupMemberList(userId, id, pageNum, pageSize);
    }

    @GetMapping("/getNoticeListByGroupId/{id}/{pageNum}/{pageSize}")
    public ResponseEntity getNoticeListByGroupId(@AuthenticationDetails("userId") Long userId, @PathVariable("id") Long id,
                                                 @PathVariable("pageNum") Integer pageNum, @PathVariable("pageSize") Integer pageSize) {
        return groupService.getNoticeListByGroupId(userId, id, pageNum, pageSize);
    }

    @PostMapping("/publishNewNotice")
    public ResponseEntity publishNewNotice(@AuthenticationDetails("userId") Long userId, @RequestBody PublishNewNoticeDto publishNewNoticeDto) {
        return groupService.publishNewNotice(userId, publishNewNoticeDto);
    }

    @PostMapping("/removeNotice/{id}")
    public ResponseEntity removeNotice(@AuthenticationDetails("userId") Long userId, @PathVariable("id") Long id) {
        return groupService.removeNotice(userId, id);
    }

    @PostMapping("/updateGroupNoSpeakStatus/{id}/{noSpeakStatus}")
    public ResponseEntity updateGroupNoSpeakStatus(@AuthenticationDetails("userId") Long userId, @PathVariable("id") Long id, @PathVariable("noSpeakStatus") Integer noSpeakStatus) {
        return groupService.updateGroupNoSpeakStatus(userId, id, noSpeakStatus);
    }

    @GetMapping("/getCandidateMemberList/{id}")
    public ResponseEntity getCandidateMemberList(@AuthenticationDetails("userId") Long userId, @PathVariable("id") Long id) {
        return groupService.getCandidateMemberList(userId, id);
    }

    @PostMapping("/transferGroup")
    public ResponseEntity transferGroup(@AuthenticationDetails("userId") Long userId, @RequestBody TransferGroupDto transferGroupDto) {
        return groupService.transferGroup(userId, transferGroupDto);
    }

    @PostMapping("/dismissGroup/{id}")
    public ResponseEntity dismissGroup(@AuthenticationDetails("userId") Long userId, @PathVariable("id") Long id) {
        return groupService.dismissGroup(userId, id);
    }

    @PostMapping("/inviteFriendToJoinGroup")
    public ResponseEntity inviteFriendToJoinGroup(@AuthenticationDetails("userId") Long userId, @RequestBody InviteFriendJoinGroupDto inviteFriendToJoinGroupDto) {
        return groupService.inviteFriendToJoinGroup(userId, inviteFriendToJoinGroupDto);
    }

    @PostMapping("/removeGroupMember")
    public ResponseEntity removeGroupMember(@AuthenticationDetails("userId") Long userId, @RequestBody RemoveGroupMemberDto removeGroupMemberDto) {
        return groupService.removeGroupMember(userId, removeGroupMemberDto);
    }

    @PostMapping("/exitGroup/{id}")
    public ResponseEntity exitGroup(@AuthenticationDetails("userId") Long userId, @PathVariable("id") Long id) {
        return groupService.exitGroup(userId, id);
    }

    @PostMapping("/applyAddGroup")
    public ResponseEntity applyAddGroup(@AuthenticationDetails("userId") Long userId, @RequestBody ApplyAddGroupDto applyAddGroupDto) {
        return groupService.applyAddGroup(userId, applyAddGroupDto);
    }

    @GetMapping("/getGroupNotificationList/{pageNum}/{pageSize}")
    public ResponseEntity getGroupNotificationList(@AuthenticationDetails("userId") Long userId, @PathVariable("pageNum") int pageNum,
                                                   @PathVariable("pageSize") int pageSize) {
        return groupService.getGroupNotificationList(userId, pageNum, pageSize);
    }

    @PostMapping("/agreeJoinGroupApply/{notificationId}")
    public ResponseEntity agreeJoinGroupApply(@AuthenticationDetails("userId") Long userId, @PathVariable("notificationId") Long notificationId) {
        return groupService.agreeJoinGroupApply(userId, notificationId);
    }

    @PostMapping("/refuseJoinGroupApply/{notificationId}")
    public ResponseEntity refuseJoinGroupApply(@AuthenticationDetails("userId") Long userId, @PathVariable("notificationId") Long notificationId) {
        return groupService.refuseJoinGroupApply(userId, notificationId);
    }

    @PostMapping("/agreeGroupInvite/{notificationId}")
    public ResponseEntity agreeGroupInvite(@AuthenticationDetails("userId") Long userId, @PathVariable("notificationId") Long notificationId) {
        return groupService.agreeGroupInvite(userId, notificationId);
    }

    @PostMapping("/refuseGroupInvite/{notificationId}")
    public ResponseEntity refuseGroupInvite(@AuthenticationDetails("userId") Long userId, @PathVariable("notificationId") Long notificationId) {
        return groupService.refuseGroupInvite(userId, notificationId);
    }

    @PostMapping("/deleteGroupNotification/{notificationId}")
    public ResponseEntity deleteGroupNotification(@AuthenticationDetails("userId") Long userId, @PathVariable("notificationId") Long notificationId) {
        return groupService.deleteGroupNotification(userId, notificationId);
    }

    @GetMapping("/getMemberInfo/{groupId}/{toUserId}")
    public ResponseEntity getMemberInfo(@AuthenticationDetails("userId") Long userId, @PathVariable("groupId") Long groupId, @PathVariable("toUserId") Long toUserId) {
        return groupService.getMemberInfo(userId, groupId, toUserId);
    }
}
