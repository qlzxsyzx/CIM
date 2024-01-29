package com.qlzxsyzx.web.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@TableName("t_groups")
@Data
public class Group {
    @TableId
    private Long id;

    private String name;

    private String avatarUrl;

    private String description;

    private Long roomId;

    private Integer memberCount;

    private Integer status; // 0-解散，1-正常，2-禁言，3-封禁

    private LocalDateTime createTime;

    private LocalDateTime dismissTime;

    private LocalDateTime banTime;
}
