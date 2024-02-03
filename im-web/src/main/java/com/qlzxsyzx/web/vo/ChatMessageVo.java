package com.qlzxsyzx.web.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ChatMessageVo {
    private String messageId;

    private String roomId;

    private String senderId;

    private String receiverId;

    // 0系统，1文本，2图片，3文件
    private Integer type;

    private String content;

    private String recordId;

    private LocalDateTime createTime;
}
