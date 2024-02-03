package com.qlzxsyzx.web.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ApplyFriendMessageVo {
    private String id;

    private UserInfoVo userInfo;

    private String toUserId;

    private String applyReason;

    private Integer status;

    private LocalDateTime createTime;
}
