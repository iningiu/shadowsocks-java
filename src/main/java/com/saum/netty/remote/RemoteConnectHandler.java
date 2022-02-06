package com.saum.netty.remote;

import com.saum.crypto.Crypto;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @Author saum
 * @Description:
 */
@Slf4j
public class RemoteConnectHandler extends ChannelInboundHandlerAdapter {

    private final Crypto crypto;
    private final ByteBuf clientCacheBuf;

    public RemoteConnectHandler(String host, int port, final ChannelHandlerContext clientCtx, Crypto crypto, ByteBuf clientCacheBuf){
        this.crypto = crypto;
        this.clientCacheBuf = clientCacheBuf;
        init(host, port, clientCtx, clientCacheBuf);
    }

    private void init(String host, int port, final ChannelHandlerContext clientCtx, final ByteBuf clientCacheBuf){
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(clientCtx.channel().eventLoop())
                .channel(NioSocketChannel.class)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5*1000)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new RemoteAccessHandler(clientCtx, crypto, clientCacheBuf));
                    }
                });

        try {
            ChannelFuture future = bootstrap.connect(InetAddress.getByName(host), port);
            future.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture channelFuture) throws Exception {
                    if(future.isSuccess()){
                        log.info("successfully connect to {}:{}", host, port);
                    }else{
                        log.info("error connect to {}:{}", host, port);
                        clientCtx.close();
                    }
                }
            });
        } catch (UnknownHostException e) {
            log.error("远程代理连接到目标地址{}:{}时出错, {}", host, port, e.getMessage(), e);
            clientCtx.close();
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = (ByteBuf) msg;
        if(buf.readableBytes() <= 0){
            log.error("远程代理接收到的请求数据为空！");
        }
        byte[] data = ByteBufUtil.getBytes(buf);
        byte[] decrypted = crypto.decrypt(data);
        clientCacheBuf.writeBytes(decrypted);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.info("disconnected with {}", ctx.channel());
        ctx.close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
    }
}
