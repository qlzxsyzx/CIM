package com.qlzxsyzx.common.mq;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SystemNotificationForChannel {
    private String channelId;

    private SystemNotification systemNotification;

    public SystemNotificationForChannel(SystemNotification systemNotification, String channelId) {
        this.systemNotification = systemNotification;
        this.channelId = channelId;
    }
}
