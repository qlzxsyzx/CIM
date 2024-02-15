package com.qlzxsyzx.web.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qlzxsyzx.web.entity.GroupNotification;
import com.qlzxsyzx.web.mapper.GroupNotificationMapper;
import com.qlzxsyzx.web.service.GroupNotificationService;
import com.qlzxsyzx.web.vo.GroupNotificationVo;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class GroupNotificationServiceImpl extends ServiceImpl<GroupNotificationMapper, GroupNotification> implements GroupNotificationService {
    @Override
    public void deleteApplyNotification(Long userId, Long groupId) {
        update().set("is_delete", 1).eq("user_id", userId).eq("group_id", groupId).eq("type", 0).update();
    }

    @Override
    public List<GroupNotificationVo> getGroupNotificationList(Long userId, int pageNum, int pageSize) {
        IPage<GroupNotification> groupNotificationIPage = new Page<>(pageNum, pageSize);
        List<GroupNotification> records = query().eq("decision_user_id", userId).eq("is_delete", 0)
                .orderByDesc("update_time")
                .page(groupNotificationIPage).getRecords();
        return records.stream().map(this::convertToVo).collect(Collectors.toList());
    }

    @Override
    public GroupNotification getGroupNotificationById(Long notificationId) {
        return query().eq("id", notificationId).eq("is_delete", 0)
                .one();
    }

    public GroupNotificationVo convertToVo(GroupNotification groupNotification) {
        GroupNotificationVo groupNotificationVo = new GroupNotificationVo();
        BeanUtils.copyProperties(groupNotification, groupNotificationVo);
        return groupNotificationVo;
    }
}
