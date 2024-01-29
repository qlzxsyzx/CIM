package com.qlzxsyzx.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qlzxsyzx.common.ResponseEntity;
import com.qlzxsyzx.user.entity.UserInfo;

import java.util.List;

public interface UserInfoService extends IService<UserInfo> {
    UserInfo getUserInfoByUsername(String username);

    UserInfo getUserInfoByUserId(Long userId);

    List<UserInfo> getBatchUserInfo(List<Long> userIdList);

}
