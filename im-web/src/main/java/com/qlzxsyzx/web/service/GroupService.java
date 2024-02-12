package com.qlzxsyzx.web.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qlzxsyzx.common.ResponseEntity;
import com.qlzxsyzx.web.dto.*;
import com.qlzxsyzx.web.entity.Group;

import java.util.List;
import java.util.Map;

public interface GroupService extends IService<Group> {
    Group getGroupById(Long groupId);

    ResponseEntity getGroupList(Long userId);

    ResponseEntity createGroup(Long userId, CreateGroupDto createGroupDto);

    Group getGroupByRoomId(Long roomId);

    ResponseEntity getGroupInfo(Long userId, Long groupId);

    ResponseEntity updateGroupRemark(Long userId, UpdateGroupRemarkDto updateGroupRemarkDto);

    ResponseEntity updateUserNickName(Long userId, UpdateUserNickNameDto updateUserNickNameDto);

    ResponseEntity updateGroupPromptStatus(Long userId, Long id, Integer status);

    ResponseEntity updateGroupName(Long userId, UpdateGroupNameDto updateGroupNameDto);

    ResponseEntity updateGroupAvatar(Long userId, UpdateGroupAvatarDto updateGroupAvatarDto);

    ResponseEntity getGroupMemberList(Long userId, Long id, Integer pageNum, Integer pageSize);

    ResponseEntity getNoticeListByGroupId(Long userId, Long id, Integer pageNum, Integer pageSize);

    ResponseEntity publishNewNotice(Long userId, PublishNewNoticeDto publishNewNoticeDto);

    ResponseEntity removeNotice(Long userId, Long id);

    ResponseEntity updateGroupNoSpeakStatus(Long userId, Long id, Integer noSpeakStatus);

    ResponseEntity getCandidateMemberList(Long userId, Long id);

    ResponseEntity transferGroup(Long userId, TransferGroupDto transferGroupDto);

    ResponseEntity dismissGroup(Long userId, Long id);

    ResponseEntity inviteFriendToJoinGroup(Long userId, InviteFriendJoinGroupDto inviteFriendToJoinGroupDto);

    ResponseEntity removeGroupMember(Long userId, RemoveGroupMemberDto removeGroupMemberDto);

    ResponseEntity exitGroup(Long userId, Long id);
}
