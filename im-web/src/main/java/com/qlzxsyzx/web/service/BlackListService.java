package com.qlzxsyzx.web.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qlzxsyzx.common.ResponseEntity;
import com.qlzxsyzx.web.entity.BlackListItem;
import org.springframework.security.core.Authentication;

public interface BlackListService extends IService<BlackListItem> {
    ResponseEntity blockFriend(Long userId, Long friendId);

    ResponseEntity removeBlackList(Long userId, Long friendId);

    boolean isExistBlackList(Long userId, Long friendId);
}
