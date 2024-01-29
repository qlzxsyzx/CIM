package com.qlzxsyzx.web.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_friends")
public class Friend {
    @TableId
    private Long id;

    private Long userId;

    private Long friendId;

    private String remark;

    // 添加成功就生成
    private Long roomId;

    // 0删除，1正常，2被删除
    private Integer status;

    private LocalDateTime createTime;
}
