package com.qlzxsyzx.web.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class GroupNoticeVo {
    private Long id;

    private Long groupId;

    private Long userId;

    private String content;

    private String image;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
