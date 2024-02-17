package com.qlzxsyzx.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qlzxsyzx.common.ResponseEntity;
import com.qlzxsyzx.user.dto.UpdatePasswordDto;
import com.qlzxsyzx.user.dto.UpdateUserAvatarDto;
import com.qlzxsyzx.user.dto.UpdateUserNameDto;
import com.qlzxsyzx.user.entity.UserInfo;

import java.util.List;

public interface UserInfoService extends IService<UserInfo> {
    UserInfo getUserInfoByUsername(String username);

    UserInfo getUserInfoByUserId(Long userId);

    List<UserInfo> getBatchUserInfo(List<Long> userIdList);

    ResponseEntity updateUserName(Long userId, UpdateUserNameDto updateUserNameDto);

    ResponseEntity updateUserAvatar(Long userId, UpdateUserAvatarDto updateUserAvatarDto);

    ResponseEntity updateUserGender(Long userId, Integer gender);

    ResponseEntity updateUserPassword(Long userId, UpdatePasswordDto updatePasswordDto);
}
