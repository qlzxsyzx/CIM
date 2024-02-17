package com.qlzxsyzx.user.dto;

import lombok.Data;

@Data
public class UpdatePasswordDto {
    /**
     * 旧密码
     */
    private String oldPassword;
    /**
     * 新密码
     */
    private String newPassword;
}
