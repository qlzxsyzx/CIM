package com.qlzxsyzx.web.vo;

import lombok.Data;

@Data
public class ChatItemVo {
    // 当前RecentChat记录
    private RecentChatVo recentChat;
    // 当前群组信息
    private GroupVo group;
    // 当前群成员信息
    private GroupMemberVo groupMember;
    // 当前好友信息
    private FriendVo friend;
    // 当前用户信息
    private UserInfoVo userInfo;
    // 当前聊天室最新一条聊天记录
    private ChatMessageVo lastMessage;
}
