package com.qlzxsyzx.web.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_apply_friend_message")
public class ApplyFriendMessage {
    @TableId
    private Long id;

    private Long userId;

    @TableField("to_user_id")
    private Long toUserId;

    private String remark;

    private String applyReason;

    private Integer status; // 0-未处理 1-已同意 -1已拒绝，2-已过期

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private Integer isSelfDelete;

    private Integer isToUserDelete;
}
