package com.qlzxsyzx.web.vo;

import lombok.Data;

@Data
public class GroupMemberItemVo {
    private GroupMemberVo member;// 成员信息
    private UserInfoVo userInfo;// 用户信息
}
