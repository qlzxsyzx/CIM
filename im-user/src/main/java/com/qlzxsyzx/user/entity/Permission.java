package com.qlzxsyzx.user.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@TableName("t_permission")
@Data
public class Permission {
    @TableId
    private Integer permissionId;

    private String permissionName;

    private String description;
}
