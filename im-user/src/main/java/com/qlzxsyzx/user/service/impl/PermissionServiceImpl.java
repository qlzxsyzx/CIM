package com.qlzxsyzx.user.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qlzxsyzx.user.entity.Permission;
import com.qlzxsyzx.user.mapper.PermissionMapper;
import com.qlzxsyzx.user.service.PermissionService;
import org.springframework.stereotype.Service;

@Service
public class PermissionServiceImpl extends ServiceImpl<PermissionMapper, Permission> implements PermissionService {
}
