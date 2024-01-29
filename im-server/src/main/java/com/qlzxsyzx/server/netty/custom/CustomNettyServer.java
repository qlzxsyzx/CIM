package com.qlzxsyzx.server.netty.custom;

import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import com.alibaba.nacos.api.PropertyKeyConst;
import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingService;
import com.qlzxsyzx.server.netty.NettyServer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Properties;

@Component
@Slf4j
public class CustomNettyServer implements NettyServer {
    private final EventLoopGroup boss = new NioEventLoopGroup(1);
    private final EventLoopGroup worker = new NioEventLoopGroup();
    @Value("${netty.port}")
    private int port;

    @Value("${netty.application.name}")
    private String appName;

    @Resource
    private NacosDiscoveryProperties nacosDiscoveryProperties;

    @Resource
    private CustomChannelInitializer customChannelInitializer;

    private void registerNettyService() {
        try {
            Properties properties = new Properties();
            properties.put(PropertyKeyConst.SERVER_ADDR, nacosDiscoveryProperties.getServerAddr());
            properties.put(PropertyKeyConst.NAMESPACE, nacosDiscoveryProperties.getNamespace());
            NamingService namingService = NamingFactory.createNamingService(properties);
            InetAddress address = InetAddress.getLocalHost();
            namingService.registerInstance(appName, address.getHostAddress(), port);
        } catch (Exception e) {
            log.error("register netty service error", e);
        }
    }

    @Override
    public void start() {
        ServerBootstrap bootstrap;
        try {
            bootstrap = new ServerBootstrap();
            bootstrap.group(boss, worker);
            bootstrap.channel(NioServerSocketChannel.class);
            bootstrap.localAddress(new InetSocketAddress(port));
            bootstrap.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
            bootstrap.childOption(ChannelOption.SO_KEEPALIVE, true);
            bootstrap.childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
            bootstrap.childHandler(customChannelInitializer);
            ChannelFuture channelFuture = bootstrap.bind().sync();
            registerNettyService();
            log.info("========netty server start=========");
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            log.error("netty server start error", e);
        } finally {
            destroy();
        }
    }

    @Override
    public void destroy() {
        boss.shutdownGracefully();
        worker.shutdownGracefully();
    }
}
