package com.qlzxsyzx.web.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qlzxsyzx.common.ResponseEntity;
import com.qlzxsyzx.web.dto.CreateGroupDto;
import com.qlzxsyzx.web.entity.Group;
import com.qlzxsyzx.web.vo.GroupVo;

import java.util.List;
import java.util.Map;

public interface GroupService extends IService<Group> {
    Group getGroupById(Long groupId);

    ResponseEntity getGroupList(Long userId);

    ResponseEntity createGroup(Long userId, CreateGroupDto createGroupDto);

    Group getGroupByRoomId(Long roomId);

    Map<Long, GroupVo> getGroupIdAndGroupVoMap(List<Long> groupIds);
}
