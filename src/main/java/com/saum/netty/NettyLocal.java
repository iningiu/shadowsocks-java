package com.saum.netty;

import com.saum.config.Config;
import com.saum.netty.local.Socks5CmdRequesthandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.socksx.v5.Socks5CommandRequestDecoder;
import io.netty.handler.codec.socksx.v5.Socks5InitialRequestDecoder;
import io.netty.handler.codec.socksx.v5.Socks5ServerEncoder;
import lombok.extern.slf4j.Slf4j;

/**
 * @Author saum
 * @Description:
 */
@Slf4j
public class NettyLocal {

    private Config config;

    public NettyLocal(Config config){
        this.config = config;
    }

    public void start(){
        EventLoopGroup boss = new NioEventLoopGroup(1);
        EventLoopGroup worker = new NioEventLoopGroup();
        ServerBootstrap serverBootstrap = new ServerBootstrap();

        try {
            serverBootstrap.group(boss, worker)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {

                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(Socks5ServerEncoder.DEFAULT);
                            ch.pipeline().addLast(new Socks5InitialRequestDecoder());
                            ch.pipeline().addLast(new Socks5CommandRequestDecoder());
                            ch.pipeline().addLast(new Socks5CmdRequesthandler(config));
                        }
                    });
            ChannelFuture channelFuture = serverBootstrap.bind(config.getLocalPort()).sync();
            log.info("本地代理服务端连接到端口[{}]", config.getLocalPort());
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            log.error("本地代理服务端启动出错");
        } finally {
            boss.shutdownGracefully();
            worker.shutdownGracefully();
        }
    }

    public static void main(String[] args) {

    }
}
