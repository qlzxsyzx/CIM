package com.qlzxsyzx.web.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@TableName("t_group_notice")
@Data
public class GroupNotice {
    @TableId
    private Long id;

    private Long groupId;

    private Long userId;

    private String content;

    private String image;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private Integer isDelete;
}
