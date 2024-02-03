package com.qlzxsyzx.web.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qlzxsyzx.common.ResponseEntity;
import com.qlzxsyzx.web.dto.CreateGroupDto;
import com.qlzxsyzx.web.entity.ChatRoom;
import com.qlzxsyzx.web.entity.Group;
import com.qlzxsyzx.web.entity.GroupMember;
import com.qlzxsyzx.web.entity.RecentChat;
import com.qlzxsyzx.web.feign.IdGeneratorClient;
import com.qlzxsyzx.web.mapper.GroupMapper;
import com.qlzxsyzx.web.service.ChatRoomService;
import com.qlzxsyzx.web.service.GroupMemberService;
import com.qlzxsyzx.web.service.GroupService;
import com.qlzxsyzx.web.service.RecentChatService;
import com.qlzxsyzx.web.vo.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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

    @Override
    public Group getGroupById(Long groupId) {
        return query().eq("id", groupId).one();
    }

    @Override
    public ResponseEntity getGroupList(Long userId) {
        // 先在group_member表中查询该用户所在的群组
        List<GroupMember> groupMembers = groupMemberService.query()
                .eq("user_id", userId)
                .eq("exit_type", 0).list();
        if (groupMembers.isEmpty()) {
            return ResponseEntity.success(new ArrayList<>());
        }
        // 获取群组id列表
        List<Long> groupIds = groupMembers.stream().map(GroupMember::getGroupId).collect(Collectors.toList());
        // 根据群组id列表查询群组列表
        List<Group> groups = query().in("id", groupIds).ne("status", 0).list();
        Map<Long, GroupVo> groupVoMap = groups.stream().collect(Collectors.toMap(Group::getId, this::convertToGroupVo));
        List<GroupItemVo> groupItemVos = new ArrayList<>();
        for (GroupMember groupMember : groupMembers) {
            GroupItemVo groupItemVo = new GroupItemVo();
            groupItemVo.setGroup(groupVoMap.get(groupMember.getGroupId()));
            groupItemVo.setGroupSetting(groupMemberService.convertToGroupSettingVo(groupMember));
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
        GroupVo groupVo = new GroupVo();
        BeanUtils.copyProperties(group, groupVo);
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
    public Map<Long, GroupVo> getGroupIdAndGroupVoMap(List<Long> groupIds) {
        List<Group> groups = query().in("id", groupIds).list();
        return groups.stream().collect(Collectors.toMap(Group::getId, this::convertToGroupVo));
    }

    private GroupVo convertToGroupVo(Group group) {
        GroupVo groupVo = new GroupVo();
        BeanUtils.copyProperties(group, groupVo);
        return groupVo;
    }
}
