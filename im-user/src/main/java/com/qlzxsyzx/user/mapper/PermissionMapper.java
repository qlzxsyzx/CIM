package com.qlzxsyzx.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qlzxsyzx.user.entity.Permission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface PermissionMapper extends BaseMapper<Permission> {
    @Select("select * from t_permission where permission_id in (select permission_id from t_role_permission where role_id = #{roleId})")
    List<Permission> selectPermissionByRoleId(Integer roleId);
}
