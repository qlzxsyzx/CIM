package com.qlzxsyzx.web.vo;

import lombok.Data;

@Data
public class GroupSettingVo {
    private String id;

    private String groupId;

    private  String userId;

    private String groupNickName;

    private Integer status; // 0 不提醒，1 正常, 2不显示
}
