package com.qlzxsyzx.web.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qlzxsyzx.common.ResponseEntity;
import com.qlzxsyzx.web.entity.BlackListItem;
import com.qlzxsyzx.web.feign.IdGeneratorClient;
import com.qlzxsyzx.web.mapper.BlackListMapper;
import com.qlzxsyzx.web.service.BlackListService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class BlackListServiceImpl extends ServiceImpl<BlackListMapper, BlackListItem> implements BlackListService {
    @Override
    public boolean isExistBlackList(Long userId, Long friendId) {
        // 查询黑名单列表，判断是否已存在
        BlackListItem blackListItem = query().eq("user_id", userId).eq("black_user_id", friendId).one();
        return blackListItem != null;
    }

    @Override
    public BlackListItem getBlackListItem(Long userId, Long friendId) {
        return query().eq("user_id", userId).eq("black_user_id", friendId).one();
    }
}
