package com.qlzxsyzx.web.dto;

import lombok.Data;

@Data
public class CreateMessageDto {
    private Long roomId;

    private Long receiverId;

    private Integer receiverType; //1-single,2-group

    private Integer type;

    private String content;

    private String contentText;

    private Long recordId;
}
