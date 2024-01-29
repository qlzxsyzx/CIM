package com.qlzxsyzx.user.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qlzxsyzx.user.entity.User;
import com.qlzxsyzx.user.mapper.UserMapper;
import com.qlzxsyzx.user.service.UserService;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    @Override
    public User findUserByUsername(String username) {
        return query().select("user_id",
                        "username",
                        "email",
                        "phone_number",
                        "registration_time",
                        "role_id",
                        "status")
                .eq("username", username).one();
    }
}
