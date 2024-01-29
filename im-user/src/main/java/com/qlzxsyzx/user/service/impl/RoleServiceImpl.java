package com.qlzxsyzx.user.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qlzxsyzx.user.entity.Role;
import com.qlzxsyzx.user.mapper.RoleMapper;
import com.qlzxsyzx.user.service.RoleService;
import org.springframework.stereotype.Service;

@Service
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role> implements RoleService {
}
