package com.qlzxsyzx.web.dto;

import lombok.Data;

import java.util.List;

@Data
public class RemoveGroupMemberDto {
    private Long groupId;

    private List<Long> memberIdList;
}
