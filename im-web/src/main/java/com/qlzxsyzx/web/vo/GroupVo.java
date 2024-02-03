package com.qlzxsyzx.web.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class GroupVo {
    private String id;

    private String name;

    private String avatarUrl;

    private String description;

    private String roomId;

    private Integer memberCount;

    private Integer status;

    private LocalDateTime dismissTime;

    private LocalDateTime banTime;
}
