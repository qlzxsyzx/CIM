package com.qlzxsyzx.user.controller;

import com.qlzxsyzx.common.ResponseEntity;
import com.qlzxsyzx.common.mq.SystemNotification;
import com.qlzxsyzx.common.type.NotificationCode;
import com.qlzxsyzx.common.type.NotificationType;
import com.qlzxsyzx.mq.client.RabbitMQClient;
import com.qlzxsyzx.resource.constant.TokenConstant;
import com.qlzxsyzx.resource.properties.ClientDetailsProperties;
import com.qlzxsyzx.user.dto.LoginDto;
import com.qlzxsyzx.user.dto.RegistrationDto;
import com.qlzxsyzx.user.entity.User;
import com.qlzxsyzx.user.entity.UserInfo;
import com.qlzxsyzx.user.feign.IdGeneratorClient;
import com.qlzxsyzx.user.service.UserInfoService;
import com.qlzxsyzx.user.service.UserService;
import com.qlzxsyzx.user.utils.IPUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
@RequestMapping("/auth")
@Transactional
public class LoginController {
    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ClientDetailsProperties clientDetailsProperties;

    @Autowired
    private UserService userService;

    @Autowired
    private UserInfoService userInfoService;

    @Autowired
    private IdGeneratorClient idGeneratorClient;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @PostMapping("/login")
    public ResponseEntity login(@RequestBody LoginDto loginDto, HttpServletRequest request, HttpServletResponse response) throws Exception {
        String username = loginDto.getUsername();
        String password = loginDto.getPassword();
        String loginPlatform = loginDto.getLoginPlatform();
        log.info("login start... username:{}", username);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> multiValueMap = new LinkedMultiValueMap<>();
        multiValueMap.add("grant_type", "password");
        multiValueMap.add("client_id", clientDetailsProperties.getClientId());
        multiValueMap.add("client_secret", clientDetailsProperties.getClientSecret());
        multiValueMap.add("username", username);
        multiValueMap.add("password", password);
        HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<>(multiValueMap, headers);
        org.springframework.http.ResponseEntity<OAuth2AccessToken> oauth2AccessToken;
        try {
            oauth2AccessToken = restTemplate.postForEntity(clientDetailsProperties.getTokenUri(), httpEntity, OAuth2AccessToken.class);
        } catch (Exception e) {
            log.error("login error... username:{}", username, e);
            return ResponseEntity.fail("用户名或密码错误");
        }
        UserInfo userInfo = userInfoService.getUserInfoByUsername(username);
        Assert.notNull(userInfo, "userInfo not found");
        userInfo.setLoginPlatform(loginPlatform);
        String realIP = IPUtil.getRealIP(request);
        userInfo.setLoginIp(realIP);
        userInfo.setLoginAddress(IPUtil.getRealAddress(realIP));
        userInfo.setLastLoginTime(LocalDateTime.now());
        userInfo.setLoginPlatform(loginPlatform);
        userInfoService.updateById(userInfo);
        log.info("login end... username:{}", username);
        // response设置cookie
        setCookie(response, TokenConstant.ACCESS_TOKEN_NAME, oauth2AccessToken.getBody().getValue(), TokenConstant.ACCESS_TOKEN_EXPIRE);
        setCookie(response, TokenConstant.REFRESH_TOKEN_NAME, oauth2AccessToken.getBody().getRefreshToken().getValue(), TokenConstant.REFRESH_TOKEN_EXPIRE);
        setCookie(response, "cim_user_id", userInfo.getUserId().toString(), -1);
        setCookie(response, "cim_platform", loginPlatform, -1);
        // redis中设置token,单点登录
        setRedisToken(userInfo.getUserId(), loginPlatform, oauth2AccessToken.getBody().getValue());
        return ResponseEntity.success(oauth2AccessToken.getBody());
    }

    private void setCookie(HttpServletResponse response, String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setPath("/");
        cookie.setMaxAge(maxAge);
        cookie.setHttpOnly(true);
        response.addCookie(cookie);
    }

    private void removeCookie(HttpServletResponse response, String name) {
        Cookie cookie = new Cookie(name, null);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        cookie.setHttpOnly(true);
        response.addCookie(cookie);
    }

    private String getRedisToken(Long userId, String platform) {
        return redisTemplate.opsForValue().get("token:" + userId + ":" + platform);
    }

    private void setRedisToken(Long userId, String platform, String token) {
        // 设置token , 多种客户端可登录，但是同种客户端只能登录一个
        redisTemplate.opsForValue().set("token:" + userId + ":" + platform, token, TokenConstant.ACCESS_TOKEN_EXPIRE, TimeUnit.SECONDS);
    }

    @PostMapping("/logout")
    public ResponseEntity logout(HttpServletRequest request, HttpServletResponse response) {
        // 获取cookie中的信息
        String accessToken = getCookieValue(request, TokenConstant.ACCESS_TOKEN_NAME);
        String userId = getCookieValue(request, "cim_user_id");
        String cimPlatform = getCookieValue(request, "cim_platform");
        // cas清除redis中的token
        casDeleteKey("token:" + userId + ":" + cimPlatform, accessToken);
        // 清除cookie中的token
        removeCookie(response, TokenConstant.ACCESS_TOKEN_NAME);
        removeCookie(response, TokenConstant.REFRESH_TOKEN_NAME);
        removeCookie(response, "cim_user_id");
        removeCookie(response, "cim_platform");
        return ResponseEntity.success("退出成功");
    }

    private String getCookieValue(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(name)) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    private void casDeleteKey(String key, String expectValue) {
        String script =
                "if redis.call('get', KEYS[1]) == ARGV[1] then " +
                        "return redis.call('del', KEYS[1]) " +
                        "else " +
                        "return 0 " +
                        "end";
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>(script, Long.class);
        redisTemplate.execute(redisScript, Collections.singletonList(key), expectValue);
    }

    @PostMapping("/register")
    public ResponseEntity register(@RequestBody RegistrationDto registrationDto) {
        String username = registrationDto.getUsername();
        String password = registrationDto.getPassword();
        String name = registrationDto.getName();
        Integer gender = registrationDto.getGender();
        String avatarUrl = registrationDto.getAvatarUrl();
        log.info("register start... username:{}", username);
        User user = userService.findUserByUsername(username);
        if (user != null) {
            log.info("register fail: username already exists... username:{}", username);
            return ResponseEntity.fail("用户名已存在");
        }
        Long userId = idGeneratorClient.generate();
        user = new User();
        user.setUserId(userId);
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setRegistrationTime(LocalDateTime.now());
        user.setRoleId(2);
        user.setStatus(1);
        UserInfo userInfo = new UserInfo();
        userInfo.setUserId(userId);
        userInfo.setUsername(username);
        userInfo.setName(name);
        userInfo.setGender(gender);
        userInfo.setAvatarUrl(avatarUrl);
        userService.save(user);
        userInfoService.save(userInfo);
        log.info("register success... username:{}", username);
        return ResponseEntity.success("注册成功");
    }
}
