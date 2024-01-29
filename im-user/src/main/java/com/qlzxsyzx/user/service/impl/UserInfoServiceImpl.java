package com.qlzxsyzx.user.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qlzxsyzx.common.ResponseEntity;
import com.qlzxsyzx.user.entity.UserInfo;
import com.qlzxsyzx.user.mapper.UserInfoMapper;
import com.qlzxsyzx.user.service.UserInfoService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserInfoServiceImpl extends ServiceImpl<UserInfoMapper, UserInfo> implements UserInfoService {
    @Override
    public UserInfo getUserInfoByUsername(String username) {
        return query().eq("username", username).one();
    }

    @Override
    public UserInfo getUserInfoByUserId(Long userId) {
        return query().eq("user_id", userId).one();
    }

    @Override
    public List<UserInfo> getBatchUserInfo(List<Long> userIdList) {
        return query().in("user_id", userIdList).list();
    }
}
