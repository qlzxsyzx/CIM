package com.qlzxsyzx.web.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_group_notification")
public class GroupNotification {
    @TableId
    private Long id;

    private Long userId;

    private Long groupId;

    private Long decisionUserId;

    private Integer decisionUserRole;

    private Integer type;  // 0:申请，1:群解散通知，2:退群通知，3:任命管理员，4:撤销管理员，5:转让群主,6:邀请入群通知,7:踢出群通知，8:申请入群成功通知，9:申请入群被拒绝通知

    private String reason;

    private Integer status;  // 0:待处理，1:已同意，2:已拒绝，3:已加入

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private Integer isDelete;
}
