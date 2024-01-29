package com.qlzxsyzx.resource.entity;

public class UserDetails {
    private Long userId;

    public UserDetails() {
    }

    public UserDetails(Long userId) {
        this.userId = userId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
