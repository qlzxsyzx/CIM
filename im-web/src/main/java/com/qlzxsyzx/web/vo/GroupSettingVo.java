package com.qlzxsyzx.web.vo;

import lombok.Data;

@Data
public class GroupSettingVo {
    private Long id;

    private Long groupId;

    private  Long userId;

    private Integer role; // 1普通，2管理员，3群主

    private String groupNickName;

    private String userNickName;

    private Integer status; // 0 不提醒，1 正常, 2不显示
}
