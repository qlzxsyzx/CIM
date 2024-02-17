package com.qlzxsyzx.user.controller;

import com.qlzxsyzx.common.ResponseEntity;
import com.qlzxsyzx.resource.annotation.AuthenticationDetails;
import com.qlzxsyzx.user.dto.UpdatePasswordDto;
import com.qlzxsyzx.user.dto.UpdateUserAvatarDto;
import com.qlzxsyzx.user.dto.UpdateUserNameDto;
import com.qlzxsyzx.user.entity.UserInfo;
import com.qlzxsyzx.user.service.UserInfoService;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/userInfo")
public class UserInfoController {

    @Autowired
    private UserInfoService userInfoService;

    @GetMapping("/getUserInfoByUsername/{username}")
    public UserInfo getUserInfoByUsername(@PathVariable("username") String username) {
        return userInfoService.getUserInfoByUsername(username);
    }

    @GetMapping("/getUserInfoByUserId/{userId}")
    public UserInfo getUserInfoByUserId(@PathVariable("userId") Long userId) {
        return userInfoService.getUserInfoByUserId(userId);
    }

    @PostMapping("/getBatchUserInfo")
    public List<UserInfo> getBatchUserInfo(@RequestBody List<Long> userIdList) {
        return userInfoService.getBatchUserInfo(userIdList);
    }

    @PostMapping("/updateUserName")
    public ResponseEntity updateUserName(@AuthenticationDetails("userId") Long userId, @RequestBody UpdateUserNameDto updateUserNameDto) {
        return userInfoService.updateUserName(userId, updateUserNameDto);
    }

    @PostMapping("/updateUserAvatar")
    public ResponseEntity updateUserAvatar(@AuthenticationDetails("userId") Long userId, @RequestBody UpdateUserAvatarDto updateUserAvatarDto) {
        return userInfoService.updateUserAvatar(userId, updateUserAvatarDto);
    }

    @PostMapping("/updateUserGender/{gender}")
    public ResponseEntity updateUserGender(@AuthenticationDetails("userId") Long userId, @PathVariable("gender") Integer gender) {
        return userInfoService.updateUserGender(userId, gender);
    }

    @PostMapping("/updateUserPassword")
    public ResponseEntity updateUserPassword(@AuthenticationDetails("userId") Long userId, @RequestBody UpdatePasswordDto updatePasswordDto) {
        return userInfoService.updateUserPassword(userId, updatePasswordDto);
    }
}
