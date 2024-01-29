package com.qlzxsyzx.user.dto;

import lombok.Data;

@Data
public class RegistrationDto {
    private String username;
    private String password;
    private String name;
    private Integer gender;
    private String avatarUrl;
}
