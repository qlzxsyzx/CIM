package com.qlzxsyzx.common.type;

public enum NotificationCode {
    // 通知类型
    LOGIN_DIFF_LOCATION(1),
    SYSTEM(2);

    private final Integer value;

    private NotificationCode(Integer value) {
        this.value = value;
    }

    public Integer getValue() {
        return value;
    }
}
