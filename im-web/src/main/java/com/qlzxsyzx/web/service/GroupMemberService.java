package com.qlzxsyzx.web.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qlzxsyzx.web.entity.GroupMember;
import com.qlzxsyzx.web.vo.GroupMemberVo;

import java.util.List;
import java.util.Map;

public interface GroupMemberService extends IService<GroupMember> {
    GroupMember getByUserIdAndGroupId(Long userId, Long groupId);

    Map<Long, GroupMemberVo> getGroupIdAndGroupMemberVoMap(Long userId, List<Long> groupIds);
}
