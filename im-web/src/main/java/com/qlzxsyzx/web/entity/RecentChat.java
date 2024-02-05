package com.qlzxsyzx.web.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@TableName("t_recent_chats")
@Data
public class RecentChat {
    @TableId
    private Long id;

    private Long userId;

    private Long toUserId;

    private Long groupId;

    private Long roomId;

    private Integer type; // 0-私聊，1-群聊

    private Integer status; // 0 不提醒，1 正常

    private Integer top; // 0 不置顶，1 置顶

    private Integer unreadCount;

    private LocalDateTime createTime;
}
