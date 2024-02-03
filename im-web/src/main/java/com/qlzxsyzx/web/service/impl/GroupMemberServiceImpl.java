package com.qlzxsyzx.web.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qlzxsyzx.web.entity.GroupMember;
import com.qlzxsyzx.web.mapper.GroupMemberMapper;
import com.qlzxsyzx.web.service.GroupMemberService;
import com.qlzxsyzx.web.vo.GroupMemberVo;
import com.qlzxsyzx.web.vo.GroupSettingVo;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class GroupMemberServiceImpl extends ServiceImpl<GroupMemberMapper, GroupMember> implements GroupMemberService {
    @Override
    public GroupMember getByUserIdAndGroupId(Long userId, Long groupId) {
        return query().eq("user_id", userId).eq("group_id", groupId).one();
    }

    @Override
    public Map<Long, GroupSettingVo> getGroupIdAndGroupSettingVoMap(Long userId, List<Long> groupIds) {
        List<GroupMember> list = query().eq("user_id", userId).in("group_id", groupIds).list();
        return list.stream().collect(Collectors.toMap(GroupMember::getGroupId, this::convertToGroupSettingVo));
    }

    @Override
    public GroupSettingVo convertToGroupSettingVo(GroupMember groupMember) {
        GroupSettingVo groupSettingVo = new GroupSettingVo();
        BeanUtils.copyProperties(groupMember, groupSettingVo);
        return groupSettingVo;
    }

    @Override
    public GroupMemberVo convertToGroupMemberVo(GroupMember groupMember) {
        GroupMemberVo groupMemberVo = new GroupMemberVo();
        BeanUtils.copyProperties(groupMember, groupMemberVo);
        return groupMemberVo;
    }
}
