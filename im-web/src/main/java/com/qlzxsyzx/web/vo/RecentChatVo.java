package com.qlzxsyzx.web.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RecentChatVo {
    private String id;

    private String userId;

    private String toUserId;

    private String groupId;

    private String roomId;

    private Integer type;

    private Integer status;

    private Integer unreadCount;

    private LocalDateTime createTime;
}
