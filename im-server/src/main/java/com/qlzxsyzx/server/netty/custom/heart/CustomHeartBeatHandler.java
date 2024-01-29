package com.qlzxsyzx.server.netty.custom.heart;


import com.qlzxsyzx.server.netty.channel.ChannelMapService;
import com.qlzxsyzx.server.netty.proto.CustomMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CustomHeartBeatHandler extends ChannelInboundHandlerAdapter {
    private final ChannelMapService channelMapService;

    public CustomHeartBeatHandler(ChannelMapService channelMapService) {
        this.channelMapService = channelMapService;
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state() == IdleState.READER_IDLE) {
                // 读空闲，关闭无用的channel，节省内存
                channelMapService.removeChannel(ctx.channel());
                ctx.channel().close();
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (!(msg instanceof CustomMessage.Message)) {
            super.channelRead(ctx, msg);
            return;
        }
        CustomMessage.Message message = (CustomMessage.Message) msg;
        if (CustomMessage.HeadType.KEEP_ALIVE_REQUEST.getNumber() != message.getHeadType()) {
            super.channelRead(ctx, msg);
            return;
        }
        // 收到心跳，返回pong
        CustomMessage.Message.Builder pong = CustomMessage.Message.newBuilder();
        pong.setHeadType(CustomMessage.HeadType.KEEP_ALIVE_RESPONSE.getNumber());
        CustomMessage.KeepAliveResponse.Builder keepAliveResponse = CustomMessage.KeepAliveResponse.newBuilder();
        keepAliveResponse.setCode(200);
        keepAliveResponse.setMsg("pong");
        CustomMessage.KeepAliveResponse build = keepAliveResponse.build();
        pong.setKeepAliveResponse(build);
        ctx.writeAndFlush(pong.build());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        // 移除channel
        channelMapService.removeChannel(ctx.channel());
        super.exceptionCaught(ctx, cause);
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        channelMapService.removeChannel(ctx.channel());
        super.handlerRemoved(ctx);
    }
}
