package com.qlzxsyzx.web.vo;

import lombok.Data;

@Data
public class GroupMemberItemVo {
    private Long id;

    private Long groupId;

    private Long userId;

    private Integer role; // 1普通，2管理员，3群主

    private String userNickName;

    private UserInfoVo userInfo;// 用户信息
}
