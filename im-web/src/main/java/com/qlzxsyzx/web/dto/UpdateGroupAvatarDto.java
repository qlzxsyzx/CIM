package com.qlzxsyzx.web.dto;

import lombok.Data;

@Data
public class UpdateGroupAvatarDto {
    private Long groupId;

    private String avatarUrl;
}
