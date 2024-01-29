package com.qlzxsyzx.server.netty.custom;

import com.qlzxsyzx.server.netty.channel.ChannelMapService;
import com.qlzxsyzx.server.netty.custom.handler.CustomProtobufDecoder;
import com.qlzxsyzx.server.netty.custom.handler.LoginRequestHandler;
import com.qlzxsyzx.server.netty.custom.heart.CustomHeartBeatHandler;
import com.qlzxsyzx.server.netty.proto.CustomMessage;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import io.netty.handler.timeout.IdleStateHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
public class CustomChannelInitializer extends ChannelInitializer<SocketChannel> {
    @Autowired
    private LoginRequestHandler loginRequestHandler;

    @Autowired
    private ChannelMapService channelMapService;

    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        // 添加基于HTTP的编解码器
        socketChannel.pipeline().addLast(new HttpServerCodec());
        socketChannel.pipeline().addLast(new HttpObjectAggregator(65536));
        socketChannel.pipeline().addLast(new WebSocketServerProtocolHandler("/ws", null, true));
        // 协议包解码
        socketChannel.pipeline().addLast(new MessageToMessageDecoder<WebSocketFrame>() {
            @Override
            protected void decode(ChannelHandlerContext channelHandlerContext, WebSocketFrame webSocketFrame, List<Object> list) throws Exception {
                ByteBuf content = webSocketFrame.content();
                list.add(content);
                content.retain();
            }
        });
        // 协议包编码
        socketChannel.pipeline().addLast(new MessageToMessageEncoder<CustomMessage.Message>() {
            @Override
            protected void encode(ChannelHandlerContext channelHandlerContext, CustomMessage.Message message, List<Object> list) throws Exception {
                ByteBuf byteBuf =
                        Unpooled.wrappedBuffer(message.toByteArray());
                list.add(new BinaryWebSocketFrame(byteBuf));
            }
        });
        // 添加自定义的Protobuf解码器
        socketChannel.pipeline().addLast(new ProtobufDecoder(CustomMessage.Message.getDefaultInstance()));

        socketChannel.pipeline().addLast("login", loginRequestHandler);
        socketChannel.pipeline().addLast("idle", new IdleStateHandler(11, 0, 0, TimeUnit.SECONDS));
        socketChannel.pipeline().addLast("heartbeat", new CustomHeartBeatHandler(channelMapService));
    }
}
