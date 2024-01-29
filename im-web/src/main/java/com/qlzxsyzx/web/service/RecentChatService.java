package com.qlzxsyzx.web.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qlzxsyzx.common.ResponseEntity;
import com.qlzxsyzx.web.entity.RecentChat;
import com.qlzxsyzx.web.vo.ChatItemVo;
import com.qlzxsyzx.web.vo.RecentChatVo;

public interface RecentChatService extends IService<RecentChat> {

    RecentChatVo convertToVo(RecentChat recentChat);
}
