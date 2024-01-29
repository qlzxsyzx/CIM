package com.qlzxsyzx.web.dto;

import lombok.Data;

@Data
public class ApplyFriendDto {
    private Long toUserId;

    private String remark;

    private String applyReason;
}
