package com.saum.netty.local;

import com.saum.crypto.Crypto;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * @Author saum
 * @Description:
 */
@Slf4j
public class Remote2LocalHandler extends SimpleChannelInboundHandler<ByteBuf> {
    private ChannelHandlerContext channelHandlerContext;
    private Crypto crypto;
    private boolean isProxy = true;

    public Remote2LocalHandler(ChannelHandlerContext channelHandlerContext, Crypto crypto, boolean isProxy){
        this.channelHandlerContext = channelHandlerContext;
        this.crypto = crypto;
        this.isProxy = isProxy;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
        if(isProxy){
            byte[] encrypted = ByteBufUtil.getBytes(msg);
            byte[] decrypted = crypto.decrypt(encrypted);
            channelHandlerContext.writeAndFlush(Unpooled.copiedBuffer(decrypted));
        }else{
            channelHandlerContext.writeAndFlush(msg);
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.info("关闭连接");
        channelHandlerContext.channel().close();
    }
}
