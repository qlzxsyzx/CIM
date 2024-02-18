package com.qlzxsyzx.web.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_chat_messages")
public class ChatMessage {
    @TableId
    private Long messageId;

    private Long roomId;

    private Long senderId;

    private Long receiverId;

    private Integer receiverType;

    // 0系统，1文本，2图片，3文件
    private Integer type;

    private String content;

    private String contentText;

    private Long recordId;

    // 0未发送，1已发送，2撤回
    private Integer status;

    private LocalDateTime createTime;
}
