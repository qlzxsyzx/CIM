package com.qlzxsyzx.web.dto;

import lombok.Data;

import java.util.List;

@Data
public class CreateGroupDto {
    private String name;

    private String avatarUrl;

    private List<Long> memberList;
}
