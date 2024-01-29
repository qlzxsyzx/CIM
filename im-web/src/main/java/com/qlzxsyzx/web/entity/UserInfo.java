package com.qlzxsyzx.web.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("t_user_info")
public class UserInfo {
    @TableId
    private Long userId;

    private String username;

    private String name;

    // 1男0女
    private Integer gender;

    private LocalDate birthday;

    private LocalDateTime lastLoginTime;

    private String avatarUrl;

    private String loginAddress;

    private String loginPlatform;

    private String loginIp;
}
