package com.saum.netty.local;

import com.saum.config.Config;
import com.saum.crypto.Crypto;
import com.saum.crypto.CryptoFactory;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.socksx.v5.*;
import lombok.extern.slf4j.Slf4j;

/**
 * @Author saum
 * @Description:
 */
@Slf4j
public class Socks5CmdRequesthandler extends SimpleChannelInboundHandler<DefaultSocks5CommandRequest> {

    private Config config;
    private Crypto crypto;
    private boolean isProxy = true;
    private EventLoopGroup bossGroup = new NioEventLoopGroup();

    public Socks5CmdRequesthandler(Config config){
        this.config = config;
        crypto = CryptoFactory.create(config.getMethod(), config.getPassword());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DefaultSocks5CommandRequest msg) throws Exception {
        if(!msg.dstAddr().equals("www.baidu.com")) return;
        log.info("远程代理服务器：{}, {}, {}, {}", msg.type(), msg.dstAddrType().toString(), msg.dstAddr(), msg.dstPort());
        if(msg.type().equals(Socks5CommandType.CONNECT)){
            log.info("准备连接远程代理服务器");

            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(bossGroup)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new Remote2LocalHandler(ctx, crypto, isProxy));
                        }
                    });
            ChannelFuture future;
            if(isProxy){
                future = bootstrap.connect(config.getServer(), config.getServerPort());
            }else{
                future = bootstrap.connect(msg.dstAddr(), msg.dstPort());
            }

            future.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture channelFuture) throws Exception {
                    Socks5CommandResponse commandResponse = null;
                    if(future.isSuccess()){
                        log.info("成功连接远程代理服务器");
                        ctx.pipeline().addLast(new Local2RemoteHandler(future, crypto, msg, isProxy));
                        commandResponse = new DefaultSocks5CommandResponse(Socks5CommandStatus.SUCCESS, Socks5AddressType.IPv4);
                    }else{
                        commandResponse = new DefaultSocks5CommandResponse(Socks5CommandStatus.FAILURE, Socks5AddressType.IPv4);
                    }
                    ctx.writeAndFlush(commandResponse);
                }
            });
        }else{
            ctx.fireChannelRead(msg);
        }
    }
}
