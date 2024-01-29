package com.qlzxsyzx.web.service;

import com.qlzxsyzx.common.ResponseEntity;

public interface UserService {
    ResponseEntity getUserInfoByUserId(Long userId);
}
