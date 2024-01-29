package com.qlzxsyzx.server.netty.custom.handler;

import com.qlzxsyzx.common.mq.SystemNotification;
import com.qlzxsyzx.common.type.NotificationCode;
import com.qlzxsyzx.common.type.NotificationType;
import com.qlzxsyzx.server.netty.publisher.NettyPublisher;
import com.qlzxsyzx.server.netty.channel.ChannelMapService;
import com.qlzxsyzx.server.netty.proto.CustomMessage;
import com.qlzxsyzx.server.redis.RedisService;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@ChannelHandler.Sharable
@Component
@Slf4j
public class LoginRequestHandler extends ChannelInboundHandlerAdapter {

    @Autowired
    private RedisService redisService;

    @Autowired
    private ChannelMapService channelMapService;

    @Autowired
    private NettyPublisher nettyMQPublisher;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (!(msg instanceof CustomMessage.Message)) {
            super.channelRead(ctx, msg);
            return;
        }
        CustomMessage.Message message = (CustomMessage.Message) msg;
        if (CustomMessage.HeadType.LOGIN_REQUEST.getNumber() != message.getHeadType()) {
            super.channelRead(ctx, msg);
            return;
        }
        // 处理登录请求
        CustomMessage.LoginRequest loginRequest = message.getLoginRequest();
        long userId = loginRequest.getUserId();
        String platform = loginRequest.getPlatform();
        log.info("用户{}在{}平台登录成功", userId, platform);
        // 查询redis得到旧ChannelId,有就发送异地登录通知,判断map里面是否存在旧Channel,不存在就广播出去
        noticeDiffLocationLogin(ctx.channel(), userId, platform);
        // 将channelId记录到redis,map
        redisService.setUserChannel(userId, platform, ctx.channel().id().asLongText());
        channelMapService.addChannel(userId, platform, ctx.channel());
        // 返回登录响应
        CustomMessage.LoginResponse.Builder builder = CustomMessage.LoginResponse.newBuilder();
        builder.setCode(200);
        builder.setMsg("WS连接成功");
        CustomMessage.Message.Builder builder1 = CustomMessage.Message.newBuilder();
        builder1.setHeadType(CustomMessage.HeadType.LOGIN_RESPONSE.getNumber());
        builder1.setLoginResponse(builder.build());
        ctx.writeAndFlush(builder1.build());
    }

    private void noticeDiffLocationLogin(Channel channel, Long userId, String platform) {
        // 推送异地登录通知
        SystemNotification notification = new SystemNotification();
        notification.setType(NotificationType.SINGLE_NOTICE.getValue());
        notification.setCode(NotificationCode.LOGIN_DIFF_LOCATION.getValue());
        notification.setUserId(userId);
        notification.setPlatform(platform);
        notification.setContent("您的账号在异地登录，请注意账号安全");
        notification.setCreateTime(LocalDateTime.now());
        nettyMQPublisher.localNoticeUserWs(channel, userId, platform, notification);
    }
}
