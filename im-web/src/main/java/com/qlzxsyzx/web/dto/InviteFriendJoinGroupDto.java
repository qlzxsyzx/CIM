package com.qlzxsyzx.web.dto;

import lombok.Data;

import java.util.List;

@Data
public class InviteFriendJoinGroupDto {
    private List<Long> friendIdList;

    private Long groupId;
}
