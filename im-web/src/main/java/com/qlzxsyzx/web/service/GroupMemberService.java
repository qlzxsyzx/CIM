package com.qlzxsyzx.web.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qlzxsyzx.web.entity.GroupMember;
import com.qlzxsyzx.web.vo.GroupMemberVo;
import com.qlzxsyzx.web.vo.GroupSettingVo;

import java.util.List;
import java.util.Map;

public interface GroupMemberService extends IService<GroupMember> {
    GroupMember getByUserIdAndGroupId(Long userId, Long groupId);

    Map<Long, GroupSettingVo> getGroupIdAndGroupSettingVoMap(Long userId, List<Long> groupIds);

    GroupSettingVo convertToGroupSettingVo(GroupMember groupMember);

    GroupMemberVo convertToGroupMemberVo(GroupMember groupMember);

    List<GroupMember> getByGroupId(Long id, Integer pageNum, Integer pageSize);

    List<GroupMemberVo> getCandidateMemberList(Long id);
}
