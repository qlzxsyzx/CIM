package com.qlzxsyzx.web.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qlzxsyzx.common.ResponseEntity;
import com.qlzxsyzx.web.dto.CreateMessageDto;
import com.qlzxsyzx.web.dto.GetChatMessageListDto;
import com.qlzxsyzx.web.dto.GetGroupChatMessageHistoryDto;
import com.qlzxsyzx.web.dto.GetSingleChatMessageHistoryDto;
import com.qlzxsyzx.web.entity.*;
import com.qlzxsyzx.web.feign.FileFeignClient;
import com.qlzxsyzx.web.feign.IdGeneratorClient;
import com.qlzxsyzx.web.mq.MQSendService;
import com.qlzxsyzx.web.service.*;
import com.qlzxsyzx.web.vo.*;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Transactional
@Service
public class ChatServiceImpl implements ChatService {
    @Autowired
    private UserInfoService userInfoService;

    @Autowired
    private FriendService friendService;

    @Autowired
    private RecentChatService recentChatService;

    @Autowired
    private GroupService groupService;

    @Autowired
    private GroupMemberService groupMemberService;

    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private ChatRoomService chatRoomService;

    @Autowired
    private BlackListService blackListService;

    @Autowired
    private IdGeneratorClient idGeneratorClient;

    @Autowired
    private MQSendService mqSendService;

    @Autowired
    private FileFeignClient fileFeignClient;

    @Override
    public ResponseEntity getRecentChatList(Long userId) {
        List<RecentChat> recentChats = recentChatService.query().eq("user_id", userId).in("status", 0, 1).list();
        if (recentChats.isEmpty()) {
            return ResponseEntity.success(Collections.emptyList());
        }
        List<ChatItemVo> chatItemVos = new ArrayList<>();
        // 分出私聊和群聊
        List<RecentChat> singleChat = recentChats.stream().filter(recentChat -> recentChat.getType() == 0).collect(Collectors.toList());
        List<RecentChat> groupChat = recentChats.stream().filter(recentChat -> recentChat.getType() == 1).collect(Collectors.toList());
        // 私聊
        if (!singleChat.isEmpty()) {
            // 获取每个聊天室最新一条消息
            List<Long> roomIds = singleChat.stream().map(RecentChat::getRoomId).collect(Collectors.toList());
            Map<Long, ChatMessageVo> latestMessageMap = chatMessageService.getRoomIdAndLatestMessageMap(roomIds);
            for (RecentChat recentChat : singleChat) {
                RecentChatVo recentChatVo = new RecentChatVo();
                BeanUtils.copyProperties(recentChat, recentChatVo);
                ChatItemVo chatItemVo = new ChatItemVo();
                chatItemVo.setRecentChat(recentChatVo);
                chatItemVo.setLastMessage(latestMessageMap.get(recentChat.getRoomId()));
                chatItemVos.add(chatItemVo);
            }
        }
        // 群聊
        if (!groupChat.isEmpty()) {
            // 获取每个群聊的最新一条消息
            List<Long> roomIds = groupChat.stream().map(RecentChat::getRoomId).collect(Collectors.toList());
            Map<Long, ChatMessageVo> latestMessageMap = chatMessageService.getRoomIdAndLatestMessageMap(roomIds);
            // 查询最新消息发送者的信息
            List<Long> senderIds = latestMessageMap.values().stream()
                    .map(ChatMessageVo::getSenderId)
                    .filter(senderId -> !senderId.equals(userId)).collect(Collectors.toList());
            // 获取userInfoVo
            Map<Long, UserInfoVo> userIdUserInfoVoMap = userInfoService.getUserIdAndUserInfoMap(senderIds);
            for (RecentChat recentChat : groupChat) {
                RecentChatVo recentChatVo = new RecentChatVo();
                BeanUtils.copyProperties(recentChat, recentChatVo);
                ChatItemVo chatItemVo = new ChatItemVo();
                chatItemVo.setRecentChat(recentChatVo);
                if (!latestMessageMap.isEmpty()) {
                    ChatMessageVo latestMessage = latestMessageMap.get(recentChat.getRoomId());
                    chatItemVo.setLastMessage(latestMessage);
                    if (!latestMessage.getSenderId().equals(userId)) {
                        GroupMemberItemVo groupMemberItemVo = new GroupMemberItemVo();
                        GroupMember member = groupMemberService.getByUserIdAndGroupId(latestMessage.getSenderId(), recentChat.getGroupId());
                        BeanUtils.copyProperties(member, groupMemberItemVo);
                        groupMemberItemVo.setUserInfo(userIdUserInfoVoMap.get(latestMessage.getSenderId()));
                        chatItemVo.setSender(groupMemberItemVo);
                    }
                }
                chatItemVos.add(chatItemVo);
            }
        }
        chatItemVos.sort((o1, o2) -> {
            LocalDateTime o1CreateTime;
            LocalDateTime o2CreateTime;
            if (o1.getLastMessage() == null) {
                o1CreateTime = o1.getRecentChat().getCreateTime();
            } else {
                o1CreateTime = o1.getLastMessage().getCreateTime();
            }
            if (o2.getLastMessage() == null) {
                o2CreateTime = o2.getRecentChat().getCreateTime();
            } else {
                o2CreateTime = o2.getLastMessage().getCreateTime();
            }
            return o2CreateTime.compareTo(o1CreateTime);
        });
        return ResponseEntity.success(chatItemVos);
    }

    @Override
    public ResponseEntity sendMessage(Long userId, CreateMessageDto createMessageDto) {
        Long roomId = createMessageDto.getRoomId();
        Integer type = createMessageDto.getReceiverType();
        // 查询聊天室是否存在
        ChatRoom chatRoom = chatRoomService.getById(roomId);
        if (chatRoom == null) {
            return ResponseEntity.fail("聊天不存在");
        }
        // 查询是否被拉黑或者删除
        if (type == 0) {
            BlackListItem blackListItem = blackListService.query().eq("user_id", createMessageDto.getReceiverId())
                    .eq("black_user_id", userId).one();
            if (blackListItem != null) {
                return ResponseEntity.fail(501, "被拉黑");
            }
            Friend friend = friendService.query().eq("user_id", userId)
                    .eq("friend_id", createMessageDto.getReceiverId()).one();
            if (friend == null) {
                return ResponseEntity.fail("好友不存在");
            } else if (friend.getStatus() == 0) {
                return ResponseEntity.fail("好友已删除");
            } else if (friend.getStatus() == 2) {
                return ResponseEntity.fail(502, "被删除");
            }
        }
        // 查询群组是否存在
        if (type == 1) {
            Group group = groupService.getGroupById(createMessageDto.getReceiverId());
            if (group == null) {
                return ResponseEntity.fail("群组不存在");
            }
            if (group.getStatus() == 0) {
                return ResponseEntity.fail("群组已解散");
            } else if (group.getStatus() == 2) {
                return ResponseEntity.fail("群组已封禁");
            }
            // 查询是否被T
            GroupMember member = groupMemberService.getByUserIdAndGroupId(userId, createMessageDto.getReceiverId());
            if (member == null) {
                return ResponseEntity.fail("你不在该群组中");
            }
            if (member.getStatus() == 2) {
                return ResponseEntity.fail("你已被移出群组");
            }
            if (group.getNoSpeak() == 1 && member.getRole() == 1) {
                return ResponseEntity.fail("群组禁止发言");
            }
        }
        // 创建消息记录
        ChatMessage chatMessage = new ChatMessage();
        BeanUtils.copyProperties(createMessageDto, chatMessage);
        chatMessage.setMessageId(idGeneratorClient.generate());
        chatMessage.setSenderId(userId);
        chatMessage.setCreateTime(LocalDateTime.now());
        chatMessageService.save(chatMessage);
        ChatMessageVo chatMessageVo = new ChatMessageVo();
        BeanUtils.copyProperties(chatMessage, chatMessageVo);
        // 发送消息
        mqSendService.asyncSendMessageVo(chatMessageVo);
        return ResponseEntity.success(chatMessageVo);
    }

    @Override
    public ResponseEntity getChatMessageList(Long userId, GetChatMessageListDto getChatMessageListDto) {
        Long roomId = getChatMessageListDto.getRoomId();
        Long lastMessageId = getChatMessageListDto.getLastMessageId();
        Integer pageSize = getChatMessageListDto.getPageSize();
        // 查询聊天室是否存在
        ChatRoom chatRoom = chatRoomService.getById(roomId);
        if (chatRoom == null) {
            return ResponseEntity.fail("聊天不存在");
        }
        Integer type = chatRoom.getType();
        if (type == 0) {
            // 私聊，判断是否是好友
            Friend friend = friendService.getFriendByUserIdAndRoomId(userId, roomId);
            if (friend == null || friend.getStatus() == 0) {
                return ResponseEntity.fail("对方不是您的好友");
            }
            // 获取最近聊天记录
            List<ChatMessage> chatMessagePage = chatMessageService.query()
                    .eq("room_id", roomId)
                    .lt(lastMessageId != null, "message_id", lastMessageId)
                    .orderByDesc("create_time")
                    .last("limit " + pageSize)
                    .list();
            return ResponseEntity.success(getFileInfo(chatMessagePage));
        }
        if (type == 1) {
            // 群聊，判断是否是群成员
            Group group = groupService.getGroupByRoomId(roomId);
            if (group == null) {
                return ResponseEntity.fail("您不是该群组成员");
            }
            // 查询我的群成员状态
            GroupMember member = groupMemberService.getByUserIdAndGroupId(userId, group.getId());
            if (member == null || member.getExitType() == 1) {
                return ResponseEntity.fail("您不是该群组成员");
            }
            // 被T，获取退出时间前的聊天记录
            LocalDateTime exitTime = member.getExitTime();
            List<ChatMessage> chatMessageList;
            if (member.getExitType() == 2) {
                chatMessageList = chatMessageService.query()
                        .eq("room_id", roomId)
                        .lt(lastMessageId != null, "message_id", lastMessageId)
                        .between("create_time", member.getJoinTime(), exitTime)
                        .orderByDesc("create_time")
                        .last("limit " + pageSize)
                        .list();
            } else {
                chatMessageList = chatMessageService.query()
                        .eq("room_id", roomId)
                        .lt(lastMessageId != null, "message_id", lastMessageId)
                        .ge("create_time", member.getJoinTime())
                        .orderByDesc("create_time")
                        .last("limit " + pageSize)
                        .list();
            }
            // 获取最近聊天记录 群聊，获取最近聊天记录
            // 转化成vo
            return ResponseEntity.success(getFileInfo(chatMessageList));
        }
        return ResponseEntity.success(new ArrayList<>());
    }

    private List<ChatMessageVo> getFileInfo(List<ChatMessage> chatMessagePage) {
        return chatMessagePage.stream()
                .map(chatMessage -> {
                    ChatMessageVo chatMessageVo = convertToVo(chatMessage);
                    if (chatMessage.getRecordId() != null) {
                        chatMessageVo.setFileInfo(fileFeignClient.getFileDetails(chatMessage.getRecordId()));
                    }
                    return chatMessageVo;
                }).collect(Collectors.toList());
    }

    @Override
    public ResponseEntity createSingleChat(Long userId, Long friendId) {
        // 先查询是否存在
        RecentChat recentChat = recentChatService.query().eq("user_id", userId).eq("to_user_id", friendId).one();
        if (recentChat != null) {
            ChatItemVo chatItemVo = new ChatItemVo();
            chatItemVo.setRecentChat(recentChatService.convertToVo(recentChat));
            return ResponseEntity.success(chatItemVo);
        }
        // 查询是否拉黑或者删除
        BlackListItem blackListItem = blackListService.query().eq("user_id", userId)
                .eq("black_user_id", friendId).one();
        if (blackListItem != null) {
            return ResponseEntity.fail("您已经拉黑该用户");
        }
        Friend friend = friendService.query().eq("user_id", userId)
                .eq("friend_id", friendId).one();
        if (friend == null) {
            return ResponseEntity.fail("好友不存在");
        } else if (friend.getStatus() == 0) {
            return ResponseEntity.fail("好友已删除");
        }
        // 创建
        recentChat = new RecentChat();
        recentChat.setId(idGeneratorClient.generate());
        recentChat.setUserId(userId);
        recentChat.setToUserId(friendId);
        recentChat.setRoomId(friend.getRoomId());
        recentChat.setType(0);
        recentChat.setStatus(friend.getStatus());
        recentChat.setCreateTime(LocalDateTime.now());
        recentChatService.save(recentChat);
        // 返回一个chatItemVo
        ChatItemVo chatItemVo = new ChatItemVo();
        chatItemVo.setRecentChat(recentChatService.convertToVo(recentChat));
        return ResponseEntity.success(chatItemVo);
    }

    @Override
    public ResponseEntity createGroupChat(Long userId, Long groupId) {
        // 先查询是否存在
        RecentChat recentChat = recentChatService.query().eq("user_id", userId).eq("group_id", groupId).one();
        if (recentChat != null) {
            ChatItemVo chatItemVo = new ChatItemVo();
            chatItemVo.setRecentChat(recentChatService.convertToVo(recentChat));
            return ResponseEntity.success(chatItemVo);
        }
        // 查询群组是否存在
        Group group = groupService.getGroupById(groupId);
        if (group == null) {
            return ResponseEntity.fail("群组不存在");
        }
        // 查询是否是群组成员
        GroupMember member = groupMemberService.getByUserIdAndGroupId(userId, groupId);
        if (member == null) {
            return ResponseEntity.fail("你不在该群组中");
        }
        if (member.getExitType() == 1) {
            return ResponseEntity.fail("你已退出该群组");
        } else if (member.getExitType() == 2) {
            return ResponseEntity.fail("你已被移出群组");
        }
        // 创建recentChat
        recentChat = new RecentChat();
        recentChat.setId(idGeneratorClient.generate());
        recentChat.setUserId(userId);
        recentChat.setGroupId(groupId);
        recentChat.setRoomId(group.getRoomId());
        recentChat.setType(1);
        recentChat.setStatus(member.getStatus());
        recentChat.setCreateTime(LocalDateTime.now());
        recentChatService.save(recentChat);
        // 返回一个chatItemVo
        ChatItemVo chatItemVo = new ChatItemVo();
        chatItemVo.setRecentChat(recentChatService.convertToVo(recentChat));
        return ResponseEntity.success(chatItemVo);
    }

    @Override
    public ResponseEntity topChat(Long userId, Long id, Integer status) {
        // 查询recentChat是否存在
        RecentChat recentChat = recentChatService.getById(id);
        if (recentChat == null || !recentChat.getUserId().equals(userId)) {
            return ResponseEntity.fail("会话不存在");
        }
        // 修改top状态
        recentChat.setTop(status);
        recentChatService.updateById(recentChat);
        return ResponseEntity.ok("修改成功");
    }

    @Override
    public ResponseEntity getSingleChatMessageHistory(Long userId, GetSingleChatMessageHistoryDto getSingleChatMessageHistoryDto) {
        Long toUserId = getSingleChatMessageHistoryDto.getToUserId();
        Long lastMessageId = getSingleChatMessageHistoryDto.getLastMessageId();
        Integer pageNum = getSingleChatMessageHistoryDto.getPageNum();
        Integer pageSize = getSingleChatMessageHistoryDto.getPageSize();
        String searchContent = getSingleChatMessageHistoryDto.getSearchContent();
        Friend friend = friendService.getFriendByUserIdAndFriendId(userId, toUserId);
        if (friend == null || friend.getStatus() == 0) {
            return ResponseEntity.fail("对方不是您的好友");
        }
        // 获取最近聊天记录
        Page<ChatMessage> page = new Page<>(pageNum, pageSize);
        Page<ChatMessage> chatMessagePage = chatMessageService.query()
                .eq("room_id", friend.getRoomId())
                .le("message_id", lastMessageId)
                .like(StringUtils.isNotBlank(searchContent), "content_text", searchContent)
                .orderByDesc("create_time")
                .page(page);
        return ResponseEntity.success(getFileInfo(chatMessagePage.getRecords()));
    }

    @Override
    public ResponseEntity getGroupChatMessageHistory(Long userId, GetGroupChatMessageHistoryDto getGroupChatMessageHistoryDto) {
        Long groupId = getGroupChatMessageHistoryDto.getGroupId();
        Long lastMessageId = getGroupChatMessageHistoryDto.getLastMessageId();
        Integer pageNum = getGroupChatMessageHistoryDto.getPageNum();
        Integer pageSize = getGroupChatMessageHistoryDto.getPageSize();
        String searchContent = getGroupChatMessageHistoryDto.getSearchContent();
        // 群聊，判断是否是群成员
        Group group = groupService.getGroupById(groupId);
        if (group == null) {
            return ResponseEntity.fail("群组不存在");
        }
        if (group.getStatus() == 2) {
            return ResponseEntity.fail("群组已封禁");
        }
        // 查询我的群成员状态
        GroupMember member = groupMemberService.getByUserIdAndGroupId(userId, groupId);
        if (member == null || member.getExitType() == 1) {
            return ResponseEntity.fail("您不是该群组成员");
        }
        // 被T，获取退出时间前的聊天记录
        LocalDateTime exitTime = member.getExitTime();
        Page<ChatMessage> page = new Page<>(pageNum, pageSize);
        Page<ChatMessage> chatMessagePage;
        if (member.getExitType() == 2) {
            chatMessagePage = chatMessageService.query()
                    .eq("room_id", group.getRoomId())
                    .le("message_id", lastMessageId)
                    .between("create_time", member.getJoinTime(), exitTime)
                    .like(StringUtils.isNotBlank(searchContent), "content_text", searchContent)
                    .orderByDesc("create_time")
                    .page(page);
        } else {
            chatMessagePage = chatMessageService.query()
                    .eq("room_id", group.getRoomId())
                    .le("message_id", lastMessageId)
                    .ge("create_time", member.getJoinTime())
                    .like(StringUtils.isNotBlank(searchContent), "content_text", searchContent)
                    .orderByDesc("create_time")
                    .page(page);
        }
        return ResponseEntity.success(getFileInfo(chatMessagePage.getRecords()));
    }

    private ChatMessageVo convertToVo(ChatMessage chatMessage) {
        ChatMessageVo chatMessageVo = new ChatMessageVo();
        BeanUtils.copyProperties(chatMessage, chatMessageVo);
        return chatMessageVo;
    }
}
