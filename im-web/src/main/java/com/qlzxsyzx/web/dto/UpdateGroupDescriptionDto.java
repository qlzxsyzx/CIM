package com.qlzxsyzx.web.dto;

import lombok.Data;

@Data
public class UpdateGroupDescriptionDto {
    private Long groupId;

    private String description; // 新的群描述
}
