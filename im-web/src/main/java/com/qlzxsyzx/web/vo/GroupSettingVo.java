package com.qlzxsyzx.web.vo;

import lombok.Data;

@Data
public class GroupSettingVo {
    private Long id;

    private Long groupId;

    private  Long userId;

    private String groupNickName;

    private Integer status; // 0 不提醒，1 正常, 2不显示
}
