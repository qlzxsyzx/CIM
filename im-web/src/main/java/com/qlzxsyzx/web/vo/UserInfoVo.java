package com.qlzxsyzx.web.vo;

import lombok.Data;

@Data
public class UserInfoVo {
    private Long userId;

    private String username;

    private String name;

    private Integer gender;

    private String avatarUrl;
}
