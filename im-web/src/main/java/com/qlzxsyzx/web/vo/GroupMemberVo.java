package com.qlzxsyzx.web.vo;

import lombok.Data;

@Data
public class GroupMemberVo {
    private String id;

    private String groupId;

    private String userId;

    private Integer role; // 1普通，2管理员，3群主

    private String userNickName;
}
