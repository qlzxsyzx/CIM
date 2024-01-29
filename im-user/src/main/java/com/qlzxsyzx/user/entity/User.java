package com.qlzxsyzx.user.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@TableName("t_user")
@Data
public class User {
    @TableId
    private Long userId;
    private String username;
    private String password;

    private String email;

    private String phoneNumber;

    private LocalDateTime registrationTime;

    private Integer roleId;

    private Integer status;
}
