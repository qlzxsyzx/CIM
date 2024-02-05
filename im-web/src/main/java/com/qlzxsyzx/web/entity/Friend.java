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

    private Long roomId;        // 添加成功就生成

    private Integer status;     // 0删除，1正常，2拉黑

    private Integer promptStatus; // 0不提示，正常

    private LocalDateTime createTime;
}
