package com.qlzxsyzx.web.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qlzxsyzx.common.ResponseEntity;
import com.qlzxsyzx.web.dto.*;
import com.qlzxsyzx.web.entity.*;
import com.qlzxsyzx.web.feign.IdGeneratorClient;
import com.qlzxsyzx.web.mapper.GroupMapper;
import com.qlzxsyzx.web.service.*;
import com.qlzxsyzx.web.vo.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        Group group = query().eq("id", groupId).ne("status", 0).one();
        if (group == null) {
            return ResponseEntity.fail("群不存在");
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
                    .orderByAsc("create_time").last("limit 10").list();
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
        if (groupMember == null || !groupMember.getUserId().equals(userId) || groupMember.getExitType() != 0) {
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
        if (group.getStatus() == 3) {
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
        if (groupMember == null || !groupMember.getUserId().equals(userId) || groupMember.getExitType() != 0) {
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
        if (group.getStatus() == 3) {
            return ResponseEntity.fail("群已被封禁");
        }
        group.setAvatarUrl(avatar);
        updateById(group);
        return ResponseEntity.success("更新成功");
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
