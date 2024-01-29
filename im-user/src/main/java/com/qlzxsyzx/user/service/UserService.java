package com.qlzxsyzx.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qlzxsyzx.user.entity.User;

public interface UserService extends IService<User> {
    User findUserByUsername(String username);
}
