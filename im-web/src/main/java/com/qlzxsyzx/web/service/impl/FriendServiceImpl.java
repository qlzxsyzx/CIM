package com.qlzxsyzx.web.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qlzxsyzx.common.ResponseEntity;
import com.qlzxsyzx.web.dto.ApplyFriendDto;
import com.qlzxsyzx.web.dto.UpdateRemarkDto;
import com.qlzxsyzx.web.entity.*;
import com.qlzxsyzx.web.feign.IdGeneratorClient;
import com.qlzxsyzx.web.mapper.FriendMapper;
import com.qlzxsyzx.web.service.*;
import com.qlzxsyzx.web.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
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

    @Autowired
    private RecentChatService recentChatService;

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
        // 检查是否有未处理的好友申请信息
        ApplyFriendMessage applyTa = applyFriendMessageService.getUntreatedApplyMessageByToUserId(userId, toUserId);
        if (applyTa != null) {
            applyTa.setApplyReason(reason);
            applyTa.setRemark(remark);
            applyTa.setCreateTime(LocalDateTime.now());
            applyFriendMessageService.updateById(applyTa);
            return ResponseEntity.success("发送成功");
        }
        // 检查是否有好友关系
        Friend friend = getFriend(userId, toUserId);
        if (friend == null) {
            // 尚未成为好友关系
            // 先判断对面是否发送了未处理的好友申请
            ApplyFriendMessage taApplyMe = applyFriendMessageService.getUntreatedApplyMessageByToUserId(toUserId, userId);
            if (taApplyMe != null) {
                // 直接添加好友，更新状态为已同意
                taApplyMe.setStatus(1);
                addFriendForNoFriendShip(userId, toUserId, remark, taApplyMe.getRemark());
                return ResponseEntity.ok("发送成功");
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
        } else {
            if (friend.getStatus() == 1) {
                return ResponseEntity.fail("你们已经是好友了");
            }
            // 恢复好友关系
            // 先判断对面是否发送了未处理的好友申请
            ApplyFriendMessage taApplyMe = applyFriendMessageService.getUntreatedApplyMessageByToUserId(toUserId, userId);
            if (taApplyMe != null) {
                // 直接添加好友，更新状态为已同意
                taApplyMe.setStatus(1);
                addFriendForFriendShip(friend, remark, taApplyMe.getRemark());
                return ResponseEntity.ok("发送成功");
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
    }

    private void addFriendForNoFriendShip(Long userId, Long friendId, String remark, String friendRemark) {
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


    private void addFriendForFriendShip(Friend myFriend, String remark, String friendRemark) {
        // 恢复好友关系
        Friend taFriend = getFriend(myFriend.getFriendId(), myFriend.getUserId());
        if (taFriend.getStatus() == 0) {
            taFriend.setPromptStatus(1);
        }
        taFriend.setStatus(1);
        taFriend.setRemark(friendRemark);
        updateById(taFriend);
        if (myFriend.getStatus() == 0) {
            myFriend.setPromptStatus(1);
        }
        myFriend.setPromptStatus(1);
        myFriend.setStatus(1);
        myFriend.setRemark(remark);
        updateById(myFriend);
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
        // 检查是否有好友关系
        Friend myFriend = getFriend(userId, applyFriendMessage.getUserId());
        if (myFriend == null) {
            addFriendForNoFriendShip(applyFriendMessage.getUserId(), userId, applyFriendMessage.getRemark(), remark);
        } else {
            addFriendForFriendShip(myFriend, remark, applyFriendMessage.getRemark());
        }
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
        // 更新申请状态为已拒绝
        applyFriendMessage.setStatus(-1);
        applyFriendMessageService.updateById(applyFriendMessage);
        return ResponseEntity.ok("处理成功");
    }

    @Override
    public ResponseEntity getFriendList(Long userId) {
        List<Friend> friendList = query().eq("user_id", userId).eq("status", 1).list();
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

    @Override
    public ResponseEntity blockUser(Long userId, Long toUserId) {
        Long blackItemId = idGeneratorClient.generate();
        // 实现黑名单逻辑
        // 先检查是否已在黑名单
        if (blackListService.isExistBlackList(userId, toUserId)) {
            return ResponseEntity.fail("已在黑名单中");
        }
        BlackListItem blackListItem = new BlackListItem();
        blackListItem.setId(blackItemId);
        blackListItem.setUserId(userId);
        blackListItem.setBlackUserId(toUserId);
        blackListService.save(blackListItem);
        // 查询是否是好友
        Friend friend = getFriend(userId, toUserId);
        if (friend != null) {
            // 删除recentChat
            recentChatService.deleteByUserIdAndRoomId(userId, friend.getRoomId());
        }
        return ResponseEntity.ok("添加黑名单成功");
    }

    @Override
    public ResponseEntity removeBlackList(Long userId, Long toUserId) {
        // 实现移除黑名单逻辑
        // 先检查是否在黑名单
        BlackListItem blackListItem = blackListService.getBlackListItem(userId, toUserId);
        if (blackListItem == null) {
            return ResponseEntity.fail("不在黑名单中");
        }
        blackListService.removeById(blackListItem.getId());
        return ResponseEntity.ok("移除黑名单成功");
    }

    @Override
    public ResponseEntity removeFriend(Long userId, Long friendId) {
        // 先判断是否是好友
        Friend friend = getFriend(userId, friendId);
        if (friend == null) {
            return ResponseEntity.fail("你们不是好友关系");
        }
        // 实现移除好友逻辑
        Friend taFriend = getFriend(friendId, userId);
        taFriend.setStatus(-1);
        updateById(taFriend);
        friend.setStatus(0);
        updateById(friend);
        // 删除recentChat
        recentChatService.deleteByUserIdAndRoomId(userId, friend.getRoomId());
        return ResponseEntity.ok("移除好友成功");
    }

    @Override
    public ResponseEntity updatePromptStatus(Long userId, Long id, Integer status) {
        // 判断是否是好友
        Friend friend = getById(id);
        if (friend == null || !friend.getUserId().equals(userId)) {
            return ResponseEntity.fail("好友不存在");
        }
        friend.setPromptStatus(status);
        updateById(friend);
        return ResponseEntity.ok("修改成功");
    }

    @Override
    public ResponseEntity getBlackList(Long userId) {
        // 查询黑名单
        List<BlackListItem> blackListItemList = blackListService.query().eq("user_id", userId).list();
        List<Long> blackUserIdList = blackListItemList.stream().map(BlackListItem::getBlackUserId).collect(Collectors.toList());
        // 查询黑名单用户信息Map
        Map<Long, UserInfoVo> userIdAndUserInfoMap = userInfoService.getUserIdAndUserInfoMap(blackUserIdList);
        // 查询friend remark
        Map<Long, FriendVo> userIdFriendVoMap = getUserIdFriendVoMap(userId, blackUserIdList);
        // 封装返回结果
        List<BlackListItemVo> blackListVoList = new ArrayList<>();
        for (BlackListItem blackListItem : blackListItemList) {
            FriendVo friendVo = userIdFriendVoMap.get(blackListItem.getBlackUserId());
            UserInfoVo userInfoVo = userIdAndUserInfoMap.get(blackListItem.getBlackUserId());
            if (friendVo != null && userInfoVo != null) {
                BlackListItemVo blackListItemVo = new BlackListItemVo();
                BeanUtils.copyProperties(blackListItem, blackListItemVo);
                blackListItemVo.setUserInfo(userInfoVo);
                blackListItemVo.setRemark(friendVo.getRemark());
                blackListVoList.add(blackListItemVo);
            }
        }
        return ResponseEntity.success(blackListVoList);
    }

    @Override
    public ResponseEntity updateRemark(Long userId, UpdateRemarkDto updateRemarkDto) {
        Long id = updateRemarkDto.getId();
        String remark = updateRemarkDto.getRemark();
        // 判断是否是好友
        Friend friend = getById(id);
        if (friend == null || !friend.getUserId().equals(userId)) {
            return ResponseEntity.fail("好友不存在");
        }
        // 更新备注
        friend.setRemark(remark);
        updateById(friend);
        return ResponseEntity.success("备注更新成功");
    }

    @Override
    public List<Friend> getFriendListByFriendIdList(List<Long> friendIdList) {
        return query().in("id", friendIdList)
                .eq("status", 0).list();
    }

    @Override
    public Friend getFriendByUserIdAndFriendId(Long userId, Long toUserId) {
        return query().eq("user_id", userId).eq("friend_id", toUserId)
                .ne("status", 0).one();
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
