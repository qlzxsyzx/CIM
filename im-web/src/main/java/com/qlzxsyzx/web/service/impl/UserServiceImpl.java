package com.qlzxsyzx.web.service.impl;

import com.qlzxsyzx.common.ResponseEntity;
import com.qlzxsyzx.web.entity.UserInfo;
import com.qlzxsyzx.web.feign.UserFeignClient;
import com.qlzxsyzx.web.service.UserService;
import com.qlzxsyzx.web.vo.UserInfoVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserFeignClient userFeignClient;

    @Override
    public ResponseEntity getUserInfoByUserId(Long userId) {
        UserInfo userInfo = userFeignClient.getUserInfoByUserId(userId);
        if (userInfo == null) {
            return ResponseEntity.fail("用户不存在");
        }
        UserInfoVo userVo = new UserInfoVo();
        BeanUtils.copyProperties(userInfo, userVo);
        return ResponseEntity.success(userVo);
    }
}
