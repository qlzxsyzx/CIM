package com.qlzxsyzx.web.dto;

import lombok.Data;

@Data
public class PublishNewNoticeDto {
    private Long noticeId;

    private Long groupId;

    private String content;

    private String imageUrl;
}
