package com.saum.netty;

import com.saum.config.Config;
import com.saum.config.ConfigLoader;
import com.saum.crypto.Crypto;
import com.saum.crypto.CryptoFactory;
import com.saum.netty.remote.AddressHandler;
import com.saum.socket.SocketMain;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

/**
 * @Author saum
 * @Description:
 */
@Slf4j
public class NettyServer {

    private Config config;

    public NettyServer(Config config) {
        this.config = config;
    }

    public void start(){
        EventLoopGroup group = new NioEventLoopGroup();
        ServerBootstrap serverBootstrap = new ServerBootstrap();

        try {
            serverBootstrap.group(group)
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, config.getTimeout())
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            Crypto crypto = CryptoFactory.create(config.getMethod(), config.getPassword());
                            ch.pipeline().addLast(new AddressHandler(crypto));
                        }
                    });

            ChannelFuture future = serverBootstrap.bind(config.getServerPort()).sync();
            log.info("远程代理服务端连接到端口[{}]", config.getServerPort());
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            log.error("远程代理服务端启动出错：{}", e.getMessage(), e);
        }finally {
            group.shutdownGracefully();
        }
    }

    public static void main(String[] args) {
        String configPath = Objects.requireNonNull(SocketMain.class.getClassLoader().getResource("config.json")).getPath();
        Config config = ConfigLoader.loadConfig(configPath);

        NettyServer nettyServer = new NettyServer(config);
        nettyServer.start();
    }
}
