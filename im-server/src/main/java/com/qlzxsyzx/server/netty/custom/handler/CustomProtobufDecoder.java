package com.qlzxsyzx.server.netty.custom.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qlzxsyzx.server.netty.proto.CustomMessage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class CustomProtobufDecoder extends ByteToMessageDecoder {
    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        byte[] bytes = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(bytes);

        CustomMessage.Message message = CustomMessage.Message.parseFrom(bytes);
        log.info("==============={}",new ObjectMapper().writeValueAsString(message));
        list.add(message);
    }
}
