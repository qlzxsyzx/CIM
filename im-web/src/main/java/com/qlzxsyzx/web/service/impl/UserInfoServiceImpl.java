package com.qlzxsyzx.web.service.impl;

import com.qlzxsyzx.web.entity.UserInfo;
import com.qlzxsyzx.web.feign.UserFeignClient;
import com.qlzxsyzx.web.service.UserInfoService;
import com.qlzxsyzx.web.vo.UserInfoVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class UserInfoServiceImpl implements UserInfoService {
    @Autowired
    private UserFeignClient userFeignClient;

    @Override
    public Map<Long, UserInfoVo> getUserIdAndUserInfoMap(List<Long> userIdList) {
        if (userIdList == null || userIdList.isEmpty()){
            return Collections.emptyMap();
        }
        List<UserInfo> batchUserInfo = userFeignClient.getBatchUserInfo(userIdList);
        return batchUserInfo.stream().collect(Collectors.toMap(UserInfo::getUserId, this::convertToUserInfoVo));
    }

    @Override
    public UserInfo getUserInfoByUsername(String username) {
        return userFeignClient.getUserInfoByUsername(username);
    }

    @Override
    public UserInfo getUserInfoByUserId(Long userId) {
        return userFeignClient.getUserInfoByUserId(userId);
    }

    @Override
    public List<UserInfo> getUserInfoList(List<Long> userIdList) {
        if (userIdList == null || userIdList.isEmpty()){
            return Collections.emptyList();
        }
        return userFeignClient.getBatchUserInfo(userIdList);
    }

    private UserInfoVo convertToUserInfoVo(UserInfo userInfo) {
        // 转换逻辑
        UserInfoVo userInfoVo = new UserInfoVo();
        BeanUtils.copyProperties(userInfo, userInfoVo);
        return userInfoVo;
    }
}
