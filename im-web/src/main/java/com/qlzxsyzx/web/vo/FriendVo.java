package com.qlzxsyzx.web.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FriendVo {
    private Long id;

    private Long userId;

    private Long friendId;

    private String remark;

    private Long roomId;

    private LocalDateTime createTime;
}
