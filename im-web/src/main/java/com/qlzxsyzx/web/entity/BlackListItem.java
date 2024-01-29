package com.qlzxsyzx.web.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@TableName("t_black_list")
@Data
public class BlackListItem {
    @TableId
    private Long id;
    private Long userId; // 用户id
    private Long blackUserId;
    private LocalDateTime createTime;
}
