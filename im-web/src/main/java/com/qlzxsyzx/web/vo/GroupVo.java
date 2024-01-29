package com.qlzxsyzx.web.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class GroupVo {
    private Long id;

    private String name;

    private String avatarUrl;

    private String description;

    private Long roomId;

    private Integer memberCount;

    private Integer status;

    private LocalDateTime dismissTime;

    private LocalDateTime banTime;
}
