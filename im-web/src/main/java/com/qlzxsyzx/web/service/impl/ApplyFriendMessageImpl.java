package com.qlzxsyzx.web.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qlzxsyzx.web.entity.ApplyFriendMessage;
import com.qlzxsyzx.web.mapper.ApplyFriendMessageMapper;
import com.qlzxsyzx.web.service.ApplyFriendMessageService;
import org.springframework.stereotype.Service;

@Service
public class ApplyFriendMessageImpl extends ServiceImpl<ApplyFriendMessageMapper, ApplyFriendMessage> implements ApplyFriendMessageService {
    @Override
    public ApplyFriendMessage getUntreatedApplyMessageByToUserId(Long userId, Long toUserId) {
        return query().eq("user_id", userId).eq("to_user_id", toUserId).eq("status", 0).eq("is_to_user_delete", 0).one();
    }

    @Override
    public ApplyFriendMessage getApplyMessageById(Long id) {
        return query().eq("id", id).eq("is_to_user_delete", 0).one();
    }
}
