package com.qlzxsyzx.web.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BlackListItemVo {
    private Long id;

    private UserInfoVo userInfo;

    private String remark;

    // 加入黑名单的时间
    private LocalDateTime createTime;
}
