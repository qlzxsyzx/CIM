package com.qlzxsyzx.web.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_chat_rooms")
public class ChatRoom {
    @TableId
    private Long roomId;

    private String name;

    //  0表示私聊，1表示群聊,2表示系统
    private Integer type;

    private LocalDateTime createTime;
}
