package com.qlzxsyzx.web.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qlzxsyzx.web.entity.ApplyFriendMessage;

public interface ApplyFriendMessageService extends IService<ApplyFriendMessage> {
    ApplyFriendMessage getUntreatedApplyMessageByToUserId(Long userId, Long toUserId);

    ApplyFriendMessage getApplyMessageById(Long id);
}
