package com.qlzxsyzx.web.dto;

import lombok.Data;

@Data
public class GetGroupChatMessageHistoryDto {
    private Long groupId;

    private Long lastMessageId; // 最后一个消息的id，第一次传0

    private String searchContent; // 搜索内容，可以为空

    private Integer pageNum;

    private Integer pageSize;
}
