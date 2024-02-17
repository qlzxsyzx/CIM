package com.qlzxsyzx.user.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qlzxsyzx.common.ResponseEntity;
import com.qlzxsyzx.user.dto.UpdatePasswordDto;
import com.qlzxsyzx.user.dto.UpdateUserAvatarDto;
import com.qlzxsyzx.user.dto.UpdateUserNameDto;
import com.qlzxsyzx.user.entity.User;
import com.qlzxsyzx.user.entity.UserInfo;
import com.qlzxsyzx.user.mapper.UserInfoMapper;
import com.qlzxsyzx.user.service.UserInfoService;
import com.qlzxsyzx.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserInfoServiceImpl extends ServiceImpl<UserInfoMapper, UserInfo> implements UserInfoService {
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserService userService;

    @Override
    public UserInfo getUserInfoByUsername(String username) {
        return query().eq("username", username).one();
    }

    @Override
    public UserInfo getUserInfoByUserId(Long userId) {
        return query().eq("user_id", userId).one();
    }

    @Override
    public List<UserInfo> getBatchUserInfo(List<Long> userIdList) {
        return query().in("user_id", userIdList).list();
    }

    @Override
    public ResponseEntity updateUserName(Long userId, UpdateUserNameDto updateUserNameDto) {
        String name = updateUserNameDto.getName();
        // 昵称不能包含空格等特殊字符且长度为2-20
        if (!name.matches("^\\S{2,20}$")) {
            return ResponseEntity.fail("昵称长度为2-20个字符，不能包含空格等特殊字符");
        }
        update().eq("user_id", userId).set("name", name).update();
        return ResponseEntity.ok("昵称更新成功");
    }

    @Override
    public ResponseEntity updateUserAvatar(Long userId, UpdateUserAvatarDto updateUserAvatarDto) {
        String avatarUrl = updateUserAvatarDto.getAvatarUrl();
        update().eq("user_id", userId).set("avatar_url", avatarUrl).update();
        return ResponseEntity.ok("头像更新成功");
    }

    @Override
    public ResponseEntity updateUserGender(Long userId, Integer gender) {
        // gender只能是 0 或 1
        if (gender != 0 && gender != 1) {
            throw new RuntimeException("参数不合法");
        }
        update().eq("user_id", userId).set("gender", gender).update();
        return ResponseEntity.ok("性别更新成功");
    }

    @Override
    public ResponseEntity updateUserPassword(Long userId, UpdatePasswordDto updatePasswordDto) {
        String oldPassword = updatePasswordDto.getOldPassword();
        String newPassword = updatePasswordDto.getNewPassword();
        // 检查旧密码是否正确
        User user = userService.getById(userId);
        if (!oldPassword.matches("^\\S{4,20}$")) {
            return ResponseEntity.fail("旧密码不正确");
        }
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            return ResponseEntity.fail("旧密码不正确");
        }
        // 校验新密码
        if (!newPassword.matches("^\\S{4,20}$")) {
            return ResponseEntity.fail("密码格式不正确");
        }
        if (newPassword.equals(oldPassword)) {
            return ResponseEntity.fail("新密码不能与旧密码相同");
        }
        // 更新密码
        String encodeNewPassword = passwordEncoder.encode(newPassword);
        userService.update().eq("user_id", userId).set("password", encodeNewPassword).update();
        return ResponseEntity.ok("更新成功");
    }
}
