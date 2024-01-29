package com.qlzxsyzx.web.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RecentChatVo {
    private Long id;

    private Long userId;

    private Long toUserId;

    private Long groupId;

    private Long roomId;

    private Integer type;

    private Integer status;

    private Integer unreadCount;

    private LocalDateTime createTime;
}
