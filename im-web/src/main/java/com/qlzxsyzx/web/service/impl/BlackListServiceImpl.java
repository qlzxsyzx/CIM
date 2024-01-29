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

    @Autowired
    private IdGeneratorClient idGeneratorClient;

    @Override
    public ResponseEntity blockFriend(Long userId, Long friendId) {
        Long blackItemId = idGeneratorClient.generate();
        // 实现黑名单逻辑
        // 先检查是否已在黑名单
        if (isExistBlackList(userId, friendId)) {
            return ResponseEntity.fail("已在黑名单中");
        }
        BlackListItem blackListItem = new BlackListItem();
        blackListItem.setId(blackItemId);
        blackListItem.setUserId(userId);
        blackListItem.setBlackUserId(friendId);
        save(blackListItem);
        return ResponseEntity.ok("添加黑名单成功");
    }

    @Override
    public ResponseEntity removeBlackList(Long userId, Long friendId) {
        // 实现移除黑名单逻辑
        // 先检查是否在黑名单
        if (!isExistBlackList(userId, friendId)) {
            return ResponseEntity.fail("不在黑名单中");
        }
        removeById(friendId);
        return ResponseEntity.ok("移除黑名单成功");
    }

    @Override
    public boolean isExistBlackList(Long userId, Long friendId) {
        // 查询黑名单列表，判断是否已存在
        BlackListItem blackListItem = query().eq("user_id", userId).eq("black_user_id", friendId).one();
        return blackListItem != null;
    }
}
