package com.qlzxsyzx.web.controller;

import com.qlzxsyzx.common.ResponseEntity;
import com.qlzxsyzx.resource.annotation.AuthenticationDetails;
import com.qlzxsyzx.web.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class UserInfoController {
    @Autowired
    private UserService userService;

    @GetMapping("/getUserInfo/{userId}")
    public ResponseEntity getUserInfo(@PathVariable("userId") Long userId){
        return userService.getUserInfoByUserId(userId);
    }
}
