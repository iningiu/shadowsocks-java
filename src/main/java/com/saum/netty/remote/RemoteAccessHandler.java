package com.saum.netty.remote;

import com.saum.crypto.Crypto;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;

/**
 * @Author saum
 * @Description:
 */
@Slf4j
public class RemoteAccessHandler extends SimpleChannelInboundHandler<ByteBuf> {

    private final ChannelHandlerContext clientCtx;
    private final Crypto crypto;
    private final ByteBuf buf;

    public RemoteAccessHandler(ChannelHandlerContext clientCtx, Crypto crypto, ByteBuf buf){
        this.clientCtx = clientCtx;
        this.crypto = crypto;
        this.buf = buf;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
//        System.out.println("!!!!!!!!!!!!!!!!!!!!:"+new String(ByteBufUtil.getBytes(buf), StandardCharsets.UTF_8));
//        log.info("远程代理替本地浏览器访问目标地址，浏览器请求为：\n{}", new String(ByteBufUtil.getBytes(buf), StandardCharsets.UTF_8));
        log.info("远程代理替本地浏览器访问目标地址，浏览器请求为：\n{}", convertByteBufToString(buf));
        ctx.writeAndFlush(buf);
    }

    private String convertByteBufToString(ByteBuf buf) {
        String str;
        if(buf.hasArray()) { // 处理堆缓冲区
            str = new String(buf.array(), buf.arrayOffset() + buf.readerIndex(), buf.readableBytes());
        } else { // 处理直接缓冲区以及复合缓冲区
            byte[] bytes = new byte[buf.readableBytes()];
            buf.getBytes(buf.readerIndex(), bytes);
            str = new String(bytes, 0, buf.readableBytes());
        }
        return str;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) {
        byte[] bytes = ByteBufUtil.getBytes(msg);
        byte[] encrypt = crypto.encrypt(bytes);
        clientCtx.writeAndFlush(Unpooled.copiedBuffer(encrypt));
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.info("disconnected with {}", ctx.channel());
        ctx.close();
        clientCtx.close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
        clientCtx.close();
    }
}
