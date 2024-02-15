package com.qlzxsyzx.web.vo;

import lombok.Data;

@Data
public class GroupMemberSimpleInfoVo {
    private Long id;
    private Long userId;
    private Long groupId;
    private Integer role;
    private String name;
    private String remark;
    private String userNickName;
    private String avatarUrl;
}
