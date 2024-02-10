package com.qlzxsyzx.web.vo;

import lombok.Data;

import java.util.List;

@Data
public class GroupInfoVo {
    private Long id;

    private String name;

    private String avatarUrl;

    private String description;

    private Long roomId;

    private Integer memberCount;

    private Integer status;

    private GroupSettingVo groupSetting;

    private List<String> memberAvatarUrl;

    private GroupNoticeVo latestNotice;
}
