package com.qlzxsyzx.user.controller;

import com.qlzxsyzx.common.ResponseEntity;
import com.qlzxsyzx.resource.annotation.AuthenticationDetails;
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
    public UserInfo getUserInfoByUsername(@PathVariable("username") String username){
        return userInfoService.getUserInfoByUsername(username);
    }

    @GetMapping("/getUserInfoByUserId/{userId}")
    public UserInfo getUserInfoByUserId(@PathVariable("userId") Long userId){
        return userInfoService.getUserInfoByUserId(userId);
    }

    @PostMapping("/getBatchUserInfo")
    public List<UserInfo> getBatchUserInfo(@RequestBody List<Long> userIdList){
        return userInfoService.getBatchUserInfo(userIdList);
    }
}
