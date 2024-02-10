package com.qlzxsyzx.web.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@TableName("t_group_members")
@Data
public class GroupMember {
    @TableId
    private Long id;

    private Long groupId;

    private  Long userId;

    private Integer role; // 1普通，2管理员，3群主

    private String groupNickName;

    private String userNickName;

    private Integer status; // 0 不提醒，1 正常

    private LocalDateTime joinTime;

    private Integer exitType; //  0，没有退出，1自愿退出，2被T

    private LocalDateTime exitTime;
}
