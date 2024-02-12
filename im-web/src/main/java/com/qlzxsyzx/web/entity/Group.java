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

    private Integer status; // 0-解散，1-正常，2-封禁

    private Integer noSpeak; // 0-不开启，1-开启

    private LocalDateTime createTime;

    private LocalDateTime dismissTime;

    private LocalDateTime banTime;
}
