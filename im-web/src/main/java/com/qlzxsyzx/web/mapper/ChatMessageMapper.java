package com.qlzxsyzx.web.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qlzxsyzx.web.entity.ChatMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ChatMessageMapper extends BaseMapper<ChatMessage> {
    List<ChatMessage> listLatestMessageGroupByRoomId(@Param("roomIds") List<Long> roomIds); // 按房间ID分组查询最新的消息列表(List<ChatMessage
}
