package com.qlzxsyzx.web.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qlzxsyzx.web.entity.GroupNotification;
import com.qlzxsyzx.web.vo.GroupNotificationVo;

import java.util.List;

public interface GroupNotificationService extends IService<GroupNotification> {
    void deleteApplyNotification(Long userId, Long groupId);

    List<GroupNotificationVo> getGroupNotificationList(Long userId, int pageNum, int pageSize);

    GroupNotification getGroupNotificationById(Long notificationId);
}
