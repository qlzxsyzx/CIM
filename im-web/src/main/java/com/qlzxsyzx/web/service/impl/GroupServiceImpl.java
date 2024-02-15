package com.qlzxsyzx.web.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qlzxsyzx.common.ResponseEntity;
import com.qlzxsyzx.web.dto.*;
import com.qlzxsyzx.web.entity.*;
import com.qlzxsyzx.web.feign.IdGeneratorClient;
import com.qlzxsyzx.web.mapper.GroupMapper;
import com.qlzxsyzx.web.service.*;
import com.qlzxsyzx.web.vo.*;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class GroupServiceImpl extends ServiceImpl<GroupMapper, Group> implements GroupService {
    @Autowired
    private GroupMemberService groupMemberService;

    @Autowired
    private IdGeneratorClient idGeneratorClient;

    @Autowired
    private ChatRoomService chatRoomService;

    @Autowired
    private RecentChatService recentChatService;

    @Autowired
    private GroupNoticeService groupNoticeService;

    @Autowired
    private UserInfoService userInfoService;

    @Autowired
    private FriendService friendService;

    @Autowired
    private GroupNotificationService groupNotificationService;

    @Override
    public Group getGroupById(Long groupId) {
        return query().eq("id", groupId).one();
    }

    @Override
    public ResponseEntity getGroupList(Long userId) {
        // 先在group_member表中查询该用户所在的群组, 没有退出和被T的
        List<GroupMember> groupMembers = groupMemberService.query()
                .eq("user_id", userId)
                .in("exit_type", Arrays.asList(0, 2)).list();
        if (groupMembers.isEmpty()) {
            return ResponseEntity.success(new ArrayList<>());
        }
        // 获取群组id列表
        List<Long> groupIds = groupMembers.stream().map(GroupMember::getGroupId).collect(Collectors.toList());
        // 根据群组id列表查询群组列表
        List<Group> groups = query().in("id", groupIds).ne("status", 0).list();
        Map<Long, GroupSettingVo> groupSettingMap = groupMemberService.getGroupIdAndGroupSettingVoMap(userId, groupIds);
        List<GroupItemVo> groupItemVos = new ArrayList<>();
        for (Group group : groups) {
            GroupItemVo groupItemVo = new GroupItemVo();
            groupItemVo.setGroup(convertToGroupVo(group));
            // 备注
            groupItemVo.setGroupSetting(groupSettingMap.get(group.getId()));
            groupItemVos.add(groupItemVo);
        }
        return ResponseEntity.success(groupItemVos);
    }

    @Override
    public ResponseEntity createGroup(Long userId, CreateGroupDto createGroupDto) {
        String groupName = createGroupDto.getName();
        String avatarUrl = createGroupDto.getAvatarUrl();
        List<Long> memberIdList = createGroupDto.getMemberList();
        // 获取id
        Long[] ids = idGeneratorClient.generateIdBatch(4 + memberIdList.size());
        // 1.创建聊天室
        ChatRoom chatRoom = new ChatRoom();
        chatRoom.setRoomId(ids[0]);
        chatRoom.setName("group_" + ids[0]);
        chatRoom.setType(1);
        chatRoomService.save(chatRoom);
        // 2.创建群组
        Group group = new Group();
        group.setId(ids[1]);
        group.setName(groupName);
        group.setAvatarUrl(avatarUrl);
        group.setRoomId(chatRoom.getRoomId());
        group.setMemberCount(memberIdList.size() + 1);
        save(group);
        // 3.创建群成员
        List<GroupMember> members = new ArrayList<>();
        for (int i = 0; i < memberIdList.size(); i++) {
            GroupMember groupMember = new GroupMember();
            groupMember.setId(ids[2 + i]);
            groupMember.setGroupId(group.getId());
            groupMember.setUserId(memberIdList.get(i));
            members.add(groupMember);
        }
        GroupMember groupMember = new GroupMember();
        groupMember.setId(ids[2 + memberIdList.size()]);
        groupMember.setGroupId(group.getId());
        groupMember.setUserId(userId);
        groupMember.setRole(3);
        members.add(groupMember);
        groupMemberService.saveBatch(members);
        // 4.给user创建一条recentChat
        RecentChat recentChat = new RecentChat();
        recentChat.setId(ids[3 + memberIdList.size()]);
        recentChat.setUserId(userId);
        recentChat.setGroupId(group.getId());
        recentChat.setRoomId(chatRoom.getRoomId());
        recentChat.setType(1);
        recentChat.setCreateTime(LocalDateTime.now());
        recentChatService.save(recentChat);
        RecentChatVo recentChatVo = new RecentChatVo();
        BeanUtils.copyProperties(recentChat, recentChatVo);
        ChatItemVo chatItemVo = new ChatItemVo();
        chatItemVo.setRecentChat(recentChatVo);
        // 返回创建的群组信息给用户
        return ResponseEntity.success(chatItemVo);
    }

    @Override
    public Group getGroupByRoomId(Long roomId) {
        return query().eq("room_id", roomId).one();
    }

    @Override
    public ResponseEntity getGroupInfo(Long userId, Long groupId) {
        // 判断群是否存在
        Group group = getGroupById(groupId);
        if (group == null || group.getStatus() == 0) {
            return ResponseEntity.fail("群不存在");
        }
        if (group.getStatus() == 2) {
            return ResponseEntity.fail("群已被封禁");
        }
        // 判断是否是群成员,展示不同信息
        GroupMember groupMember = groupMemberService.getByUserIdAndGroupId(userId, groupId);
        if (groupMember == null || groupMember.getExitType() != 0) {
            // 群成员不存在或者已退出
            NotMemberGroupInfoVo notMemberGroupInfoVo = getNotMemberGroupInfoVo(group);
            // 获取前10位群成员头像，以role排序
            List<GroupMember> members = groupMemberService.query().eq("group_id", groupId)
                    .eq("exit_type", 0)
                    .orderByDesc("role")
                    .orderByAsc("join_time").last("limit 10").list();
            // 查出头像
            List<UserInfo> userInfoList = userInfoService.getUserInfoList(
                    members.stream().map(GroupMember::getUserId).collect(Collectors.toList()));
            notMemberGroupInfoVo.setMemberAvatarUrl(userInfoList.stream().map(UserInfo::getAvatarUrl).collect(Collectors.toList()));
            return ResponseEntity.success(notMemberGroupInfoVo);
        } else {
            // 群成员存在
            GroupInfoVo groupInfoVo = getGroupInfoVo(group);
            // 我的群设置
            groupInfoVo.setGroupSetting(groupMemberService.convertToGroupSettingVo(groupMember));
            // 最新公告
            groupInfoVo.setLatestNotice(groupNoticeService.getLatestNotice(groupId));
            // 获取前10位群成员头像，以role排序
            List<GroupMember> members = groupMemberService.query().eq("group_id", groupId)
                    .eq("exit_type", 0)
                    .orderByDesc("role")
                    .orderByAsc("join_time").last("limit 10").list();
            // 查出头像
            List<UserInfo> userInfoList = userInfoService.getUserInfoList(
                    members.stream().map(GroupMember::getUserId).collect(Collectors.toList()));
            groupInfoVo
                    .setMemberAvatarUrl(userInfoList.stream().map(UserInfo::getAvatarUrl).collect(Collectors.toList()));
            return ResponseEntity.success(groupInfoVo);
        }
    }

    @Override
    public ResponseEntity updateGroupRemark(Long userId, UpdateGroupRemarkDto updateGroupRemarkDto) {
        Long id = updateGroupRemarkDto.getId();
        String remark = updateGroupRemarkDto.getRemark();
        // 先判断是否是群成员
        GroupMember groupMember = groupMemberService.getById(id);
        if (groupMember == null || !groupMember.getUserId().equals(userId) || groupMember.getExitType() != 0) {
            return ResponseEntity.fail("你不是群成员");
        }
        Group group = getGroupById(groupMember.getGroupId());
        if (group == null || group.getStatus() == 0) {
            return ResponseEntity.fail("群不存在");
        }
        if (group.getStatus() == 2) {
            return ResponseEntity.fail("群已被封禁");
        }
        // 更新备注
        groupMember.setGroupNickName(remark);
        groupMemberService.updateById(groupMember);
        return ResponseEntity.success("更新成功");
    }

    @Override
    public ResponseEntity updateUserNickName(Long userId, UpdateUserNickNameDto updateUserNickNameDto) {
        Long id = updateUserNickNameDto.getId();
        String nickName = updateUserNickNameDto.getUserNickName();
        // 先判断是否是群成员
        GroupMember groupMember = groupMemberService.getById(id);
        if (groupMember == null || !groupMember.getUserId().equals(userId) || groupMember.getExitType() != 0) {
            return ResponseEntity.fail("你不是群成员");
        }
        Group group = getGroupById(groupMember.getGroupId());
        if (group == null || group.getStatus() == 0) {
            return ResponseEntity.fail("群不存在");
        }
        if (group.getStatus() == 2) {
            return ResponseEntity.fail("群已被封禁");
        }
        // 更新备注
        groupMember.setUserNickName(nickName);
        groupMemberService.updateById(groupMember);
        return ResponseEntity.success("更新成功");
    }

    @Override
    public ResponseEntity updateGroupPromptStatus(Long userId, Long id, Integer status) {
        // 先判断是否是群成员
        GroupMember groupMember = groupMemberService.getById(id);
        if (groupMember == null || !groupMember.getUserId().equals(userId) || groupMember.getExitType() != 0) {
            return ResponseEntity.fail("你不是群成员");
        }
        Group group = getGroupById(groupMember.getGroupId());
        if (group == null || group.getStatus() == 0) {
            return ResponseEntity.fail("群不存在");
        }
        if (group.getStatus() == 2) {
            return ResponseEntity.fail("群已被封禁");
        }
        // 更新群提示状态
        groupMember.setStatus(status);
        groupMemberService.updateById(groupMember);
        return ResponseEntity.success("更新成功");
    }

    @Override
    public ResponseEntity updateGroupName(Long userId, UpdateGroupNameDto updateGroupNameDto) {
        Long groupId = updateGroupNameDto.getGroupId();
        String name = updateGroupNameDto.getName();
        // 先判断是否是群成员
        GroupMember groupMember = groupMemberService.getByUserIdAndGroupId(userId, groupId);
        if (groupMember == null || groupMember.getExitType() != 0) {
            return ResponseEntity.fail("你不是群成员");
        }
        if (groupMember.getRole() != 3) {
            return ResponseEntity.fail("无权修改");
        }
        // 更新群名称
        Group group = getGroupById(groupId);
        if (group == null || group.getStatus() == 0) {
            return ResponseEntity.fail("群不存在");
        }
        if (group.getStatus() == 2) {
            return ResponseEntity.fail("群已被封禁");
        }
        group.setName(name);
        updateById(group);
        return ResponseEntity.success("更新成功");
    }

    @Override
    public ResponseEntity updateGroupAvatar(Long userId, UpdateGroupAvatarDto updateGroupAvatarDto) {
        Long groupId = updateGroupAvatarDto.getGroupId();
        String avatar = updateGroupAvatarDto.getAvatarUrl();
        // 先判断是否是群成员
        GroupMember groupMember = groupMemberService.getByUserIdAndGroupId(userId, groupId);
        if (groupMember == null || groupMember.getExitType() != 0) {
            return ResponseEntity.fail("你不是群成员");
        }
        if (groupMember.getRole() != 3) {
            return ResponseEntity.fail("无权修改");
        }
        // 更新群头像
        Group group = getGroupById(groupId);
        if (group == null || group.getStatus() == 0) {
            return ResponseEntity.fail("群不存在");
        }
        if (group.getStatus() == 2) {
            return ResponseEntity.fail("群已被封禁");
        }
        group.setAvatarUrl(avatar);
        updateById(group);
        return ResponseEntity.success("更新成功");
    }

    @Override
    public ResponseEntity getGroupMemberList(Long userId, Long id, Integer pageNum, Integer pageSize) {
        // 判断群是否存在
        Group group = getGroupById(id);
        if (group == null || group.getStatus() == 0) {
            return ResponseEntity.fail("群不存在");
        }
        if (group.getStatus() == 2) {
            return ResponseEntity.fail("群已被封禁");
        }
        // 判断是否是群成员
        GroupMember groupMember = groupMemberService.getByUserIdAndGroupId(userId, id);
        if (groupMember == null || groupMember.getExitType() != 0) {
            return ResponseEntity.fail("你不是群成员");
        }
        // 获取群成员列表
        List<GroupMember> groupMemberList = groupMemberService.getByGroupId(id, pageNum, pageSize);
        List<GroupMemberItemVo> groupMemberItemVoList = new ArrayList<>();
        // 查询用户信息
        List<Long> userIdList = groupMemberList.stream().map(GroupMember::getUserId).collect(Collectors.toList());
        Map<Long, UserInfoVo> userIdAndUserInfoMap = userInfoService.getUserIdAndUserInfoMap(userIdList);
        for (GroupMember member : groupMemberList) {
            GroupMemberItemVo groupMemberItemVo = new GroupMemberItemVo();
            BeanUtils.copyProperties(member, groupMemberItemVo);
            groupMemberItemVo.setUserInfo(userIdAndUserInfoMap.get(member.getUserId()));
            groupMemberItemVoList.add(groupMemberItemVo);
        }
        return ResponseEntity.success(groupMemberItemVoList);
    }

    @Override
    public ResponseEntity getNoticeListByGroupId(Long userId, Long id, Integer pageNum, Integer pageSize) {
        // 判断群是否存在
        Group group = getGroupById(id);
        if (group == null || group.getStatus() == 0) {
            return ResponseEntity.fail("群不存在");
        }
        if (group.getStatus() == 2) {
            return ResponseEntity.fail("群已被封禁");
        }
        // 判断是否是群成员
        GroupMember groupMember = groupMemberService.getByUserIdAndGroupId(userId, id);
        if (groupMember == null || groupMember.getExitType() != 0) {
            return ResponseEntity.fail("你不是群成员");
        }
        // 查询群公告
        return ResponseEntity.success(groupNoticeService.getNoticesByGroupId(id, pageNum, pageSize));
    }

    @Override
    public ResponseEntity publishNewNotice(Long userId, PublishNewNoticeDto publishNewNoticeDto) {
        Long noticeId = publishNewNoticeDto.getNoticeId();
        Long groupId = publishNewNoticeDto.getGroupId();
        String content = publishNewNoticeDto.getContent();
        String imageUrl = publishNewNoticeDto.getImageUrl();
        // 判断群是否存在
        Group group = getGroupById(groupId);
        if (group == null || group.getStatus() == 0) {
            return ResponseEntity.fail("群不存在");
        }
        if (group.getStatus() == 2) {
            return ResponseEntity.fail("群已被封禁");
        }
        // 判断是否是群成员
        GroupMember groupMember = groupMemberService.getByUserIdAndGroupId(userId, groupId);
        if (groupMember == null || groupMember.getExitType() != 0) {
            return ResponseEntity.fail("你不是群成员");
        }
        if (groupMember.getRole() == 1) {
            return ResponseEntity.fail("无权操作");
        }
        // noticeId存在就是更新公告
        if (noticeId != null) {
            GroupNotice notice = groupNoticeService.getNoticeById(noticeId);
            if (notice != null) {
                notice.setUserId(userId);
                notice.setContent(content);
                notice.setImage(imageUrl);
                notice.setUpdateTime(LocalDateTime.now());
                groupNoticeService.updateById(notice);
                return ResponseEntity.success(groupNoticeService.convertToVo(notice));
            }
        }
        // 发布群公告
        GroupNotice groupNotice = new GroupNotice();
        groupNotice.setId(idGeneratorClient.generate());
        groupNotice.setGroupId(groupId);
        groupNotice.setUserId(userId);
        groupNotice.setContent(content);
        groupNotice.setImage(imageUrl);
        groupNotice.setCreateTime(LocalDateTime.now());
        groupNotice.setUpdateTime(LocalDateTime.now());
        groupNoticeService.save(groupNotice);
        return ResponseEntity.success(groupNoticeService.convertToVo(groupNotice));
    }

    @Override
    public ResponseEntity removeNotice(Long userId, Long id) {
        // 查询公告
        GroupNotice notice = groupNoticeService.getNoticeById(id);
        if (notice == null) {
            return ResponseEntity.ok("删除成功");
        }
        // 判断是否有权限
        Long groupId = notice.getGroupId();
        Group group = getGroupById(groupId);
        if (group == null || group.getStatus() == 0) {
            return ResponseEntity.fail("群不存在");
        }
        if (group.getStatus() == 2) {
            return ResponseEntity.fail("群已被封禁");
        }
        // 判断是否是群成员
        GroupMember groupMember = groupMemberService.getByUserIdAndGroupId(userId, groupId);
        if (groupMember == null || groupMember.getExitType() != 0) {
            return ResponseEntity.fail("你不是群成员");
        }
        if (groupMember.getRole() == 1) {
            return ResponseEntity.fail("无权操作");
        }
        // 删除公告
        notice.setIsDelete(1);
        groupNoticeService.updateById(notice);
        return ResponseEntity.success("删除成功");
    }

    @Override
    public ResponseEntity updateGroupNoSpeakStatus(Long userId, Long id, Integer noSpeakStatus) {
        Group group = getGroupById(id);
        if (group == null || group.getStatus() == 0) {
            return ResponseEntity.fail("群不存在");
        }
        if (group.getStatus() == 2) {
            return ResponseEntity.fail("群已被封禁");
        }
        // 判断是否是群成员
        GroupMember groupMember = groupMemberService.getByUserIdAndGroupId(userId, id);
        if (groupMember == null || groupMember.getExitType() != 0) {
            return ResponseEntity.fail("你不是群成员");
        }
        if (groupMember.getRole() == 1) {
            return ResponseEntity.fail("无权操作");
        }
        group.setNoSpeak(noSpeakStatus);
        updateById(group);
        return ResponseEntity.success("更新成功");
    }

    @Override
    public ResponseEntity getCandidateMemberList(Long userId, Long id) {
        Group group = getGroupById(id);
        if (group == null || group.getStatus() == 0) {
            return ResponseEntity.fail("群不存在");
        }
        if (group.getStatus() == 2) {
            return ResponseEntity.fail("群已被封禁");
        }
        // 判断是否是群成员
        GroupMember groupMember = groupMemberService.getByUserIdAndGroupId(userId, id);
        if (groupMember == null || groupMember.getExitType() != 0) {
            return ResponseEntity.fail("你不是群成员");
        }
        if (groupMember.getRole() != 3) {
            return ResponseEntity.fail("无权操作");
        }
        List<GroupMemberVo> groupMemberList = groupMemberService.getCandidateMemberList(id);
        return ResponseEntity.success(groupMemberList);
    }

    @Override
    public ResponseEntity transferGroup(Long userId, TransferGroupDto transferGroupDto) {
        Long groupId = transferGroupDto.getGroupId();
        Long memberId = transferGroupDto.getMemberId();
        Group group = getGroupById(groupId);
        if (group == null || group.getStatus() == 0) {
            return ResponseEntity.fail("群不存在");
        }
        if (group.getStatus() == 2) {
            return ResponseEntity.fail("群已被封禁");
        }
        // 判断是否是群成员
        GroupMember groupMember = groupMemberService.getByUserIdAndGroupId(userId, groupId);
        if (groupMember == null || groupMember.getExitType() != 0) {
            return ResponseEntity.fail("你不是群成员");
        }
        if (groupMember.getRole() != 3) {
            return ResponseEntity.fail("无权操作");
        }
        // 判断是否是群成员
        GroupMember toMember = groupMemberService.getById(memberId);
        if (toMember == null || toMember.getExitType() != 0 || !toMember.getGroupId().equals(groupId)) {
            return ResponseEntity.fail("群成员不存在");
        }
        if (toMember.getRole() != 2) {
            return ResponseEntity.fail("该成员不符合条件");
        }
        // 转让 todo，ws发送消息
        groupMember.setRole(2);
        groupMemberService.updateById(groupMember);
        toMember.setRole(3);
        groupMemberService.updateById(toMember);
        return ResponseEntity.ok("转让成功");
    }

    @Override
    public ResponseEntity dismissGroup(Long userId, Long id) {
        Group group = getGroupById(id);
        if (group == null || group.getStatus() == 0) {
            return ResponseEntity.fail("群不存在");
        }
        if (group.getStatus() == 2) {
            return ResponseEntity.fail("群已被封禁");
        }
        // 判断是否是群成员
        GroupMember groupMember = groupMemberService.getByUserIdAndGroupId(userId, id);
        if (groupMember == null || groupMember.getExitType() != 0) {
            return ResponseEntity.fail("你不是群成员");
        }
        if (groupMember.getRole() != 3) {
            return ResponseEntity.fail("无权操作");
        }
        // 解散 todo 消息
        group.setStatus(0);
        group.setDismissTime(LocalDateTime.now());
        updateById(group);
        return ResponseEntity.ok("解散成功");
    }

    @Override
    public ResponseEntity inviteFriendToJoinGroup(Long userId, InviteFriendJoinGroupDto inviteFriendToJoinGroupDto) {
        Long groupId = inviteFriendToJoinGroupDto.getGroupId();
        List<Long> friendIdList = inviteFriendToJoinGroupDto.getFriendIdList();
        Group group = getGroupById(groupId);
        if (group == null || group.getStatus() == 0) {
            return ResponseEntity.fail("群不存在");
        }
        if (group.getStatus() == 2) {
            return ResponseEntity.fail("群已被封禁");
        }
        // 判断是否是群成员
        GroupMember groupMember = groupMemberService.getByUserIdAndGroupId(userId, groupId);
        if (groupMember == null || groupMember.getExitType() != 0) {
            return ResponseEntity.fail("你不是群成员");
        }
        List<Friend> friendListByFriendIdList = friendService.getFriendListByFriendIdList(friendIdList);
        if (friendListByFriendIdList.isEmpty()) {
            return ResponseEntity.ok("邀请成功");
        }
        // 邀请 异步 todo 发送消息
        return ResponseEntity.ok("邀请成功");
    }

    @Override
    public ResponseEntity removeGroupMember(Long userId, RemoveGroupMemberDto removeGroupMemberDto) {
        Long groupId = removeGroupMemberDto.getGroupId();
        List<Long> memberIdList = removeGroupMemberDto.getMemberIdList();
        Group group = getGroupById(groupId);
        if (group == null || group.getStatus() == 0) {
            return ResponseEntity.fail("群不存在");
        }
        if (group.getStatus() == 2) {
            return ResponseEntity.fail("群已被封禁");
        }
        // 判断是否是群成员
        GroupMember groupMember = groupMemberService.getByUserIdAndGroupId(userId, groupId);
        if (groupMember == null || groupMember.getExitType() != 0) {
            return ResponseEntity.fail("你不是群成员");
        }
        if (groupMember.getRole() == 1) {
            return ResponseEntity.fail("无权操作");
        }
        groupMemberService.update().set("exit_type", 2).set("exit_time", LocalDateTime.now())
                .in("id", memberIdList).update();
        return ResponseEntity.ok("移除成功");
    }

    @Override
    public ResponseEntity exitGroup(Long userId, Long id) {
        Group group = getGroupById(id);
        if (group == null || group.getStatus() == 0) {
            return ResponseEntity.fail("群不存在");
        }
        // 判断是否是群成员
        GroupMember groupMember = groupMemberService.getByUserIdAndGroupId(userId, id);
        if (groupMember == null || groupMember.getExitType() != 0) {
            return ResponseEntity.fail("你不是群成员");
        }
        // todo 退出通知
        groupMember.setExitType(1);
        groupMember.setExitTime(LocalDateTime.now());
        groupMemberService.updateById(groupMember);
        return ResponseEntity.ok("退出成功");
    }

    @Override
    public ResponseEntity applyAddGroup(Long userId, ApplyAddGroupDto applyAddGroupDto) {
        Long groupId = applyAddGroupDto.getGroupId();
        String reason = applyAddGroupDto.getReason();
        Group group = getGroupById(groupId);
        if (group == null || group.getStatus() == 0) {
            return ResponseEntity.fail("群不存在");
        }
        if (group.getStatus() == 2) {
            return ResponseEntity.fail("群已被封禁");
        }
        // 判断是否是群成员
        GroupMember groupMember = groupMemberService.getByUserIdAndGroupId(userId, groupId);
        if (groupMember != null) {
            if (groupMember.getExitType() == 0) {
                return ResponseEntity.fail("你已经是群成员了");
            }
        }
        // 删除所有用户对该群的申请记录
        groupNotificationService.deleteApplyNotification(userId, groupId);
        // 查询群主，管理员
        List<GroupMember> adminList = groupMemberService.getAdminList(groupId);
        // 向群主，管理员发出申请
        Long[] idList = idGeneratorClient.generateIdBatch(adminList.size());
        List<GroupNotification> groupNotificationList = new ArrayList<>();
        for (int i = 0; i < adminList.size(); i++) {
            GroupMember admin = adminList.get(i);
            GroupNotification groupNotification = new GroupNotification();
            groupNotification.setId(idList[i]);
            groupNotification.setUserId(userId);
            groupNotification.setGroupId(groupId);
            groupNotification.setType(0);
            groupNotification.setDecisionUserId(admin.getUserId());
            groupNotification.setDecisionUserRole(admin.getRole());
            groupNotification.setReason(reason);
            groupNotificationList.add(groupNotification);
        }
        groupNotificationService.saveBatch(groupNotificationList);
        // todo ws
        return ResponseEntity.ok("申请已发出");
    }

    @Override
    public ResponseEntity getGroupNotificationList(Long userId, int pageNum, int pageSize) {
        return ResponseEntity.success(groupNotificationService.getGroupNotificationList(userId, pageNum, pageSize));
    }

    @Override
    public ResponseEntity agreeJoinGroupApply(Long userId, Long notificationId) {
        GroupNotification apply = groupNotificationService.getGroupNotificationById(notificationId);
        if (apply == null || apply.getType() != 0 || !apply.getDecisionUserId().equals(userId)) {
            return ResponseEntity.error("申请不存在");
        }
        if (apply.getStatus() != 0) {
            return ResponseEntity.error("申请已处理");
        }
        Long groupId = apply.getGroupId();
        Group group = getGroupById(groupId);
        if (group == null || group.getStatus() == 0) {
            return ResponseEntity.fail("群不存在");
        }
        if (group.getStatus() == 2) {
            return ResponseEntity.fail("群已被封禁");
        }
        // 判断是否是群成员
        GroupMember groupMember = groupMemberService.getByUserIdAndGroupId(userId, groupId);
        if (groupMember == null || groupMember.getExitType() != 0) {
            return ResponseEntity.fail("你不是群成员");
        }
        if (groupMember.getRole() == 1) {
            return ResponseEntity.fail("无权操作");
        }
        Long applyUserId = apply.getUserId();
        GroupMember applyMember = groupMemberService.getByUserIdAndGroupId(applyUserId, groupId);
        if (applyMember != null && applyMember.getExitType() == 0) {
            return ResponseEntity.fail("申请用户已经是群成员");
        }
        // 处理申请
        groupNotificationService.update().eq("id", notificationId).set("status", 1).set("update_time", LocalDateTime.now()).update();
        // 未处理的其他人的申请记录以及邀请记录设置为已加入
        groupNotificationService.update().set("status", 3)
                .eq("group_id", groupId)
                .eq("status", 0)
                .and(sql -> sql.nested(w1 -> w1.eq("type", 0).eq("user_id", applyUserId))
                        .or().nested(w2 -> w2.eq("type", 6).eq("decision_user_id", applyUserId)))
                .update();
        // 添加群成员
        if (applyMember != null) {
            // 删除原记录
            groupMemberService.removeById(applyMember.getId());
        }
        groupMemberService.addGroupMember(applyUserId, groupId);
        // 申请入群成功通知
        GroupNotification notification = new GroupNotification();
        notification.setId(idGeneratorClient.generate());
        notification.setUserId(userId);
        notification.setGroupId(groupId);
        notification.setType(8);
        notification.setDecisionUserId(applyUserId);
        notification.setDecisionUserRole(1);
        groupNotificationService.save(notification);
        // todo 发送消息
        return ResponseEntity.ok("处理成功");
    }

    @Override
    public ResponseEntity refuseJoinGroupApply(Long userId, Long notificationId) {
        GroupNotification apply = groupNotificationService.getGroupNotificationById(notificationId);
        if (apply == null || apply.getType() != 0 || !apply.getDecisionUserId().equals(userId)) {
            return ResponseEntity.error("申请不存在");
        }
        if (apply.getStatus() != 0) {
            return ResponseEntity.error("申请已处理");
        }
        Long groupId = apply.getGroupId();
        Group group = getGroupById(groupId);
        if (group == null || group.getStatus() == 0) {
            return ResponseEntity.fail("群不存在");
        }
        if (group.getStatus() == 2) {
            return ResponseEntity.fail("群已被封禁");
        }
        // 判断是否是群成员
        GroupMember groupMember = groupMemberService.getByUserIdAndGroupId(userId, groupId);
        if (groupMember == null || groupMember.getExitType() != 0) {
            return ResponseEntity.fail("你不是群成员");
        }
        if (groupMember.getRole() == 1) {
            return ResponseEntity.fail("无权操作");
        }
        Long applyUserId = apply.getUserId();
        GroupMember applyMember = groupMemberService.getByUserIdAndGroupId(applyUserId, groupId);
        if (applyMember != null && applyMember.getExitType() == 0) {
            return ResponseEntity.fail("申请用户已经是群成员");
        }
        // 处理申请
        groupNotificationService.update().eq("id", notificationId).set("status", 2).set("update_time", LocalDateTime.now()).update();
        // 删除其他人的申请记录
        groupNotificationService.update().set("is_delete", 1)
                .eq("group_id", groupId)
                .eq("user_id", applyUserId)
                .eq("type", 0)
                .ne("id", notificationId)
                .update();
        // 申请入群拒绝通知
        GroupNotification notification = new GroupNotification();
        notification.setId(idGeneratorClient.generate());
        notification.setUserId(userId);
        notification.setGroupId(groupId);
        notification.setType(9);
        notification.setDecisionUserId(applyUserId);
        notification.setDecisionUserRole(1);
        groupNotificationService.save(notification);
        return ResponseEntity.ok("处理成功");
    }

    @Override
    public ResponseEntity agreeGroupInvite(Long userId, Long notificationId) {
        GroupNotification invite = groupNotificationService.getGroupNotificationById(notificationId);
        if (invite == null || invite.getType() != 6 || !invite.getDecisionUserId().equals(userId)) {
            return ResponseEntity.error("申请不存在");
        }
        if (invite.getStatus() != 0) {
            return ResponseEntity.error("申请已处理");
        }
        Long groupId = invite.getGroupId();
        Group group = getGroupById(groupId);
        if (group == null || group.getStatus() == 0) {
            return ResponseEntity.fail("群不存在");
        }
        if (group.getStatus() == 2) {
            return ResponseEntity.fail("群已被封禁");
        }
        Long inviteUserId = invite.getDecisionUserId();
        GroupMember inviteMember = groupMemberService.getByUserIdAndGroupId(inviteUserId, groupId);
        if (inviteMember != null && inviteMember.getExitType() == 0) {
            return ResponseEntity.fail("你已经是群成员");
        }
        // 处理邀请
        groupNotificationService.update().eq("id", notificationId).set("status", 1).set("update_time", LocalDateTime.now()).update();
        // 未处理的申请记录以及邀请记录设置为已加入
        groupNotificationService.update().set("status", 3)
                .eq("group_id", groupId)
                .eq("status", 0)
                .and(sql -> sql.nested(w1 -> w1.eq("type", 0).eq("user_id", userId))
                        .or().nested(w2 -> w2.eq("type", 6).eq("decision_user_id", userId)))
                .update();
        // 添加群成员
        if (inviteMember != null) {
            // 删除原记录
            groupMemberService.removeById(inviteMember.getId());
        }
        groupMemberService.addGroupMember(userId, groupId);
        // todo ws发送
        return ResponseEntity.ok("处理成功");
    }

    @Override
    public ResponseEntity refuseGroupInvite(Long userId, Long notificationId) {
        GroupNotification invite = groupNotificationService.getGroupNotificationById(notificationId);
        if (invite == null || invite.getType() != 6 || !invite.getDecisionUserId().equals(userId)) {
            return ResponseEntity.error("申请不存在");
        }
        if (invite.getStatus() != 0) {
            return ResponseEntity.error("申请已处理");
        }
        Long groupId = invite.getGroupId();
        Group group = getGroupById(groupId);
        if (group == null || group.getStatus() == 0) {
            return ResponseEntity.fail("群不存在");
        }
        if (group.getStatus() == 2) {
            return ResponseEntity.fail("群已被封禁");
        }
        Long inviteUserId = invite.getDecisionUserId();
        GroupMember inviteMember = groupMemberService.getByUserIdAndGroupId(inviteUserId, groupId);
        if (inviteMember != null && inviteMember.getExitType() == 0) {
            return ResponseEntity.fail("你已经是群成员");
        }
        // 处理邀请
        groupNotificationService.update().eq("id", notificationId).set("status", 2).set("update_time", LocalDateTime.now()).update();
        // todo ws发送
        return ResponseEntity.ok("处理成功");
    }

    @Override
    public ResponseEntity deleteGroupNotification(Long userId, Long notificationId) {
        GroupNotification notification = groupNotificationService.getGroupNotificationById(notificationId);
        if (notification == null || !notification.getDecisionUserId().equals(userId)) {
            return ResponseEntity.error("申请不存在");
        }
        notification.setIsDelete(1);
        notification.setUpdateTime(LocalDateTime.now());
        groupNotificationService.updateById(notification);
        return ResponseEntity.ok("删除成功");
    }

    @Override
    public ResponseEntity getMemberInfo(Long userId, Long groupId, Long toUserId) {
        // 查询用户信息
        UserInfo userInfo = userInfoService.getUserInfoByUserId(toUserId);
        if (userInfo == null) {
            return ResponseEntity.error("用户不存在");
        }
        GroupMemberSimpleInfoVo info = new GroupMemberSimpleInfoVo();
        info.setName(userInfo.getName());
        info.setAvatarUrl(userInfo.getAvatarUrl());
        info.setUserId(toUserId);
        info.setGroupId(groupId);
        // 查询是否好友
        Friend friend = friendService.getFriendByUserIdAndFriendId(userId, toUserId);
        if (friend != null) {
            info.setRemark(friend.getRemark());
        }
        // 查询是否群成员
        GroupMember member = groupMemberService.getByUserIdAndGroupId(toUserId, groupId);
        if (member != null) {
            info.setId(member.getId());
            info.setRole(member.getRole());
            info.setUserNickName(member.getUserNickName());
        }
        return ResponseEntity.success(info);
    }

    private NotMemberGroupInfoVo getNotMemberGroupInfoVo(Group group) {
        NotMemberGroupInfoVo notMemberGroupInfoVo = new NotMemberGroupInfoVo();
        BeanUtils.copyProperties(group, notMemberGroupInfoVo);
        return notMemberGroupInfoVo;
    }

    private GroupInfoVo getGroupInfoVo(Group group) {
        GroupInfoVo groupInfoVo = new GroupInfoVo();
        BeanUtils.copyProperties(group, groupInfoVo);
        return groupInfoVo;
    }

    private GroupVo convertToGroupVo(Group group) {
        GroupVo groupVo = new GroupVo();
        BeanUtils.copyProperties(group, groupVo);
        return groupVo;
    }
}
