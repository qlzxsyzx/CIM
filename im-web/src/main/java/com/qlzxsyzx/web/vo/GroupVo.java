package com.qlzxsyzx.web.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class GroupVo {
    private Long id;

    private Long roomId;

    private String name;

    private String avatarUrl;

    private Integer memberCount;
}
