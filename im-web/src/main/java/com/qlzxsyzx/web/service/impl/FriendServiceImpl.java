package com.qlzxsyzx.web.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qlzxsyzx.common.ResponseEntity;
import com.qlzxsyzx.web.dto.ApplyFriendDto;
import com.qlzxsyzx.web.entity.ApplyFriendMessage;
import com.qlzxsyzx.web.entity.ChatRoom;
import com.qlzxsyzx.web.entity.Friend;
import com.qlzxsyzx.web.entity.UserInfo;
import com.qlzxsyzx.web.feign.IdGeneratorClient;
import com.qlzxsyzx.web.mapper.FriendMapper;
import com.qlzxsyzx.web.service.*;
import com.qlzxsyzx.web.vo.ApplyFriendMessageVo;
import com.qlzxsyzx.web.vo.FriendItemVo;
import com.qlzxsyzx.web.vo.FriendVo;
import com.qlzxsyzx.web.vo.UserInfoVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class FriendServiceImpl extends ServiceImpl<FriendMapper, Friend> implements FriendService {

    @Autowired
    private ApplyFriendMessageService applyFriendMessageService;

    @Autowired
    private ChatRoomService chatRoomService;

    @Autowired
    private IdGeneratorClient idGeneratorClient;

    @Autowired
    private BlackListService blackListService;

    @Autowired
    private UserInfoService userInfoService;


    @Override
    public ResponseEntity searchByUsername(String username) {
        log.info("friend search user start... username:{}", username);
        UserInfo userInfo = userInfoService.getUserInfoByUsername(username);
        if (userInfo == null) {
            log.info("friend search user end... user not exist username:{}", username);
            return ResponseEntity.fail("用户不存在");
        }
        UserInfoVo userVo = new UserInfoVo();
        BeanUtils.copyProperties(userInfo, userVo);
        log.info("friend search user end... user exist username:{}", username);
        return ResponseEntity.success(userVo);
    }

    private void addFriend(Long userId, Long friendId, String remark, String friendRemark) {
        Long[] ids = idGeneratorClient.generateIdBatch(3);
        // 生成一个聊天室
        ChatRoom chatRoom = new ChatRoom();
        chatRoom.setRoomId(ids[0]);
        chatRoom.setName(userId + "_" + friendId);
        chatRoom.setType(0);
        chatRoomService.save(chatRoom);
        // 添加好友
        Friend friend = new Friend();
        friend.setId(ids[1]);
        friend.setUserId(userId);
        friend.setFriendId(friendId);
        friend.setRoomId(ids[0]);
        friend.setRemark(remark);
        friend.setStatus(1);
        save(friend);
        // 添加好友
        Friend friend2 = new Friend();
        friend2.setId(ids[2]);
        friend2.setUserId(friendId);
        friend2.setFriendId(userId);
        friend2.setRemark(friendRemark);
        friend2.setRoomId(ids[0]);
        friend2.setStatus(1);
        save(friend2);
        // todo:向MQ发送消息
    }

    @Override
    public ResponseEntity applyAddFriend(Long userId, ApplyFriendDto applyFriendDto) {
        Long toUserId = applyFriendDto.getToUserId();
        String remark = applyFriendDto.getRemark();
        String reason = applyFriendDto.getApplyReason();
        if (userId.equals(toUserId)) {
            return ResponseEntity.fail("不能添加自己为好友");
        }
        // 检查是否被拉黑或者拉黑对方
        if (blackListService.isExistBlackList(userId, toUserId)) {
            return ResponseEntity.fail("对方已在黑名单，请先解除");
        }
        if (blackListService.isExistBlackList(toUserId, userId)) {
            return ResponseEntity.fail("您已被对方拉黑，无法添加");
        }
        // 检查是否有好友关系
        Friend friend = getFriend(userId, toUserId);
        if (friend != null && friend.getStatus() == 1) {
            return ResponseEntity.fail("你们已经是好友了");
        }
        // 上面判断后是可以发送申请好友的情况
        // 先判断对面是否发送了未处理的好友申请
        ApplyFriendMessage taApplyMe = applyFriendMessageService.getUntreatedApplyMessageByToUserId(toUserId, userId);
        if (taApplyMe != null) {
            // 直接添加好友，更新状态为已同意
            taApplyMe.setStatus(1);
            addFriend(userId, toUserId, remark, taApplyMe.getRemark());
            return ResponseEntity.ok("发送成功");
        }
        // 检查是否有未处理的好友申请信息
        ApplyFriendMessage applyTa = applyFriendMessageService.getUntreatedApplyMessageByToUserId(userId, toUserId);
        if (applyTa != null) {
            return ResponseEntity.fail("你已经申请过好友了");
        }
        // 发送好友申请
        ApplyFriendMessage applyFriendMessage = new ApplyFriendMessage();
        applyFriendMessage.setId(idGeneratorClient.generate());
        applyFriendMessage.setUserId(userId);
        applyFriendMessage.setToUserId(toUserId);
        applyFriendMessage.setRemark(remark);
        applyFriendMessage.setApplyReason(reason);
        applyFriendMessage.setStatus(0);
        applyFriendMessageService.save(applyFriendMessage);
        return ResponseEntity.ok("发送成功");
    }

    @Override
    public ResponseEntity agreeAddFriend(Long userId, Long applyFriendMessageId, String remark) {
        ApplyFriendMessage applyFriendMessage = applyFriendMessageService.getApplyMessageById(applyFriendMessageId);
        if (applyFriendMessage == null) {
            return ResponseEntity.fail("该申请不存在");
        }
        if (!applyFriendMessage.getToUserId().equals(userId)) {
            return ResponseEntity.fail("该申请不属于你");
        }
        if (applyFriendMessage.getStatus() != 0) {
            return ResponseEntity.fail("该申请已处理过");
        }
        // 更新申请状态为已同意
        applyFriendMessage.setStatus(1);
        applyFriendMessageService.updateById(applyFriendMessage);
        // 添加好友
        addFriend(applyFriendMessage.getUserId(), userId, applyFriendMessage.getRemark(), remark);
        return ResponseEntity.ok("添加成功");
    }

    @Override
    public ResponseEntity refuseAddFriend(Long userId, Long applyFriendMessageId) {
        ApplyFriendMessage applyFriendMessage = applyFriendMessageService.getApplyMessageById(applyFriendMessageId);
        if (applyFriendMessage == null) {
            return ResponseEntity.fail("该申请不存在");
        }
        if (!applyFriendMessage.getToUserId().equals(userId)) {
            return ResponseEntity.fail("该申请不属于你");
        }
        if (applyFriendMessage.getStatus() != 0) {
            return ResponseEntity.fail("该申请已处理过");
        }
        // 更新申请状态为已同意
        applyFriendMessage.setStatus(-1);
        applyFriendMessageService.updateById(applyFriendMessage);
        return ResponseEntity.ok("处理成功");
    }

    @Override
    public ResponseEntity getFriendList(Long userId) {
        List<Friend> friendList = query().eq("user_id", userId).in("status", Arrays.asList(1, 2)).list();
        if (friendList.isEmpty()) {
            return ResponseEntity.success(new ArrayList<>());
        }
        // 查询所有好友的userInfo
        List<Long> friendIds = friendList.stream().map(Friend::getFriendId).collect(Collectors.toList());
        Map<Long, UserInfoVo> userIdAndUserInfoMap = userInfoService.getUserIdAndUserInfoMap(friendIds);
        List<FriendItemVo> friendItemVoList = new ArrayList<>();
        for (Friend friend : friendList) {
            FriendVo friendVo = convertToFriendVo(friend);
            FriendItemVo friendItemVo = new FriendItemVo();
            friendItemVo.setFriend(friendVo);
            friendItemVo.setUserInfo(userIdAndUserInfoMap.get(friend.getFriendId()));
            friendItemVoList.add(friendItemVo);
        }
        return ResponseEntity.success(friendItemVoList);
    }

    @Override
    public Map<Long, FriendVo> getUserIdFriendVoMap(Long userId, List<Long> toUserIdList) {
        if (CollectionUtils.isEmpty(toUserIdList)) {
            return Collections.emptyMap();
        }
        List<Friend> friendList = query().eq("user_id", userId).in("friend_id", toUserIdList).list();
        return friendList.stream().collect(Collectors.toMap(Friend::getFriendId, this::convertToFriendVo));
    }

    @Override
    public ResponseEntity getApplyFriendMessageList(Long userId) {
        List<ApplyFriendMessage> messages = applyFriendMessageService.query()
                .nested(w -> w.eq("user_id", userId).eq("is_self_delete", 0)).or()
                .nested(w -> w.eq("to_user_id", userId).eq("is_to_user_delete", 0))
                .list();
        if (messages.isEmpty()) {
            return ResponseEntity.success(new ArrayList<>());
        }
        // 查询用户信息
        List<Long> userIdList = messages.stream().map(applyFriendMessage -> {
            if (applyFriendMessage.getUserId().equals(userId)) {
                return applyFriendMessage.getToUserId();
            } else {
                return applyFriendMessage.getUserId();
            }
        }).collect(Collectors.toList());
        Map<Long, UserInfoVo> userIdAndUserInfoMap = userInfoService.getUserIdAndUserInfoMap(userIdList);
        List<ApplyFriendMessageVo> applyFriendMessageVoList = new ArrayList<>();
        for (ApplyFriendMessage message : messages) {
            ApplyFriendMessageVo applyFriendMessageVo = new ApplyFriendMessageVo();
            BeanUtils.copyProperties(message, applyFriendMessageVo);
            UserInfoVo userInfo;
            if (message.getUserId().equals(userId)) {
                userInfo = userIdAndUserInfoMap.get(message.getToUserId());
            } else {
                userInfo = userIdAndUserInfoMap.get(message.getUserId());
            }
            applyFriendMessageVo.setUserInfo(userInfo);
            applyFriendMessageVoList.add(applyFriendMessageVo);
        }
        applyFriendMessageVoList.sort((o1, o2) -> o2.getCreateTime().compareTo(o1.getCreateTime()));
        return ResponseEntity.success(applyFriendMessageVoList);
    }

    @Override
    public Friend getFriendByUserIdAndRoomId(Long userId, Long roomId) {
        return query().eq("user_id", userId)
                .eq("room_id", roomId).one();
    }


    private Friend getFriend(Long userId, Long friendId) {
        return query().eq("user_id", userId).eq("friend_id", friendId).one();
    }

    private FriendVo convertToFriendVo(Friend friend) {
        FriendVo friendVo = new FriendVo();
        BeanUtils.copyProperties(friend, friendVo);
        return friendVo;
    }
}