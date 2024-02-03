package com.qlzxsyzx.web.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FriendVo {
    private String id;

    private String userId;

    private String friendId;

    private String remark;

    private String roomId;

    private LocalDateTime createTime;
}
