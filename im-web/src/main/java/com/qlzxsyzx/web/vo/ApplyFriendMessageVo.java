package com.qlzxsyzx.web.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ApplyFriendMessageVo {
    private Long id;

    private UserInfoVo userInfo;

    private Long toUserId;

    private String applyReason;

    private Integer status;

    private LocalDateTime createTime;
}
