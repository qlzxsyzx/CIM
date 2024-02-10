package com.qlzxsyzx.web.vo;

import lombok.Data;

import java.util.List;

@Data
public class NotMemberGroupInfoVo {
    private Long id;

    private String name;

    private String avatarUrl;

    private String description;

    private Integer memberCount;

    private List<String> memberAvatarUrl;
}
