package com.qlzxsyzx.web.feign;

import com.qlzxsyzx.web.entity.UserInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@FeignClient("im-user")
public interface UserFeignClient {

    @GetMapping("/userInfo/getUserInfoByUserId/{userId}")
    UserInfo getUserInfoByUserId(@PathVariable("userId") Long userId);

    @GetMapping("/userInfo/getUserInfoByUsername/{username}")
    UserInfo getUserInfoByUsername(@PathVariable("username") String username);

    @PostMapping("/userInfo/getBatchUserInfo")
    List<UserInfo> getBatchUserInfo(List<Long> userIdList);
}
