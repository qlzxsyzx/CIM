package com.qlzxsyzx.web.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ChatMessageVo {
    private Long messageId;

    private Long roomId;

    private Long senderId;

    private Long receiverId;

    private Integer receiverType;

    // 0系统，1文本，2图片，3文件
    private Integer type;

    private String content;

    private Long recordId;

    private FileDetailsVo fileInfo;

    private LocalDateTime createTime;
}
