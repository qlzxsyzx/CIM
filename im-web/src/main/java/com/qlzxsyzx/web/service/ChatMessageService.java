package com.qlzxsyzx.web.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qlzxsyzx.web.entity.ChatMessage;
import com.qlzxsyzx.web.vo.ChatMessageVo;

import java.util.List;
import java.util.Map;

public interface ChatMessageService extends IService<ChatMessage> {
    ChatMessage getLatestMessageByRoomId(Long roomId);

    Map<Long, ChatMessageVo> getFriendRoomIdAndLatestMessageMap(List<Long> roomIds);
}
