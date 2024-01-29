package com.qlzxsyzx.web.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qlzxsyzx.common.ResponseEntity;
import com.qlzxsyzx.web.dto.ApplyFriendDto;
import com.qlzxsyzx.web.entity.Friend;
import com.qlzxsyzx.web.vo.FriendVo;

import java.util.List;
import java.util.Map;

public interface FriendService extends IService<Friend> {
    ResponseEntity searchByUsername(String username);

    ResponseEntity applyAddFriend(Long userId,ApplyFriendDto applyFriendDto);

    ResponseEntity agreeAddFriend(Long userId, Long applyFriendMessageId, String remark);

    ResponseEntity refuseAddFriend(Long userId, Long applyFriendMessageId);

    ResponseEntity getFriendList(Long userId);

    Map<Long,FriendVo> getUserIdFriendVoMap(Long userId,List<Long> toUserIdList);

    ResponseEntity getApplyFriendMessageList(Long userId);

    Friend getFriendByUserIdAndRoomId(Long userId, Long roomId);
}
