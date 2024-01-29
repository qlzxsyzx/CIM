package com.qlzxsyzx.common.type;

public enum NotificationType {
    // 通知类型通知类型
    SINGLE_NOTICE(1),
    MULTI_NOTICE(2);
    private final Integer value;

    private NotificationType(Integer value) {
        this.value = value;
    }

    public Integer getValue() {
        return value;
    }

    public static NotificationType getType(Integer value) {
        for (NotificationType type : NotificationType.values()) {
            if (type.getValue().equals(value)) {
                return type;
            }
        }
        return null;
    }
}
