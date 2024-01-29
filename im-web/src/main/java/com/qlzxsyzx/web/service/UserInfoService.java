package com.qlzxsyzx.web.service;

import com.qlzxsyzx.web.entity.UserInfo;
import com.qlzxsyzx.web.vo.UserInfoVo;

import java.util.List;
import java.util.Map;

public interface UserInfoService {
    Map<Long, UserInfoVo> getUserIdAndUserInfoMap(List<Long> userIdList);

    UserInfo getUserInfoByUsername(String username);

    UserInfo getUserInfoByUserId(Long userId);
}
