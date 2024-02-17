package com.qlzxsyzx.web.dto;

import lombok.Data;

@Data
public class GetChatMessageListDto {
    private Long roomId;

    private Long lastMessageId;

    private Integer pageSize;
}
