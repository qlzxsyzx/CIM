package com.qlzxsyzx.web.vo;

import lombok.Data;

@Data
public class ChatItemVo {
    // 当前RecentChat记录
    private RecentChatVo recentChat;
    // 如果有最新消息，就显示最新消息的发送者信息
    private GroupMemberItemVo sender;
    // 当前聊天室最新一条聊天记录
    private ChatMessageVo lastMessage;
}
