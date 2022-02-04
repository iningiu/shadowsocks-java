package com.saum.netty.local;

import com.saum.crypto.Crypto;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.socksx.v5.Socks5CommandRequest;
import lombok.extern.slf4j.Slf4j;

import java.net.IDN;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * @Author saum
 * @Description:
 */
@Slf4j
public class Local2RemoteHandler extends SimpleChannelInboundHandler<ByteBuf> {

    private ChannelFuture channelFuture;
    private Crypto crypto;
    private Socks5CommandRequest socks5CommandRequest;
    private boolean isProxy = true;
    private boolean addAddress = false;

    public Local2RemoteHandler(ChannelFuture channelFuture, Crypto crypto,
                               Socks5CommandRequest socks5CommandRequest, boolean isProxy){
        this.channelFuture = channelFuture;
        this.crypto = crypto;
        this.socks5CommandRequest = socks5CommandRequest;
        this.isProxy = isProxy;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
        if(isProxy){
            if(!addAddress){
                ByteBuf addressInfo = parseAddress(socks5CommandRequest);
                addressInfo.writeBytes(msg);
                msg = addressInfo;
                addAddress = true;
            }
            byte[] plainText = ByteBufUtil.getBytes(msg);
            byte[] encrypt = crypto.encrypt(plainText);
            log.info("本地代理服务器发送给远程代理服务器");
            channelFuture.channel().writeAndFlush(Unpooled.copiedBuffer(encrypt));
        }else{
            channelFuture.channel().writeAndFlush(msg);
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.info("关闭连接");
        channelFuture.channel().close();
    }

    private ByteBuf parseAddress(Socks5CommandRequest msg) throws UnknownHostException {
        ByteBuf buf = Unpooled.buffer();
        // ATYP 字段：address type 的缩写
        byte addressType = msg.dstAddrType().byteValue();
        String host = msg.dstAddr();
        int port = msg.dstPort();
        if(addressType == 0x01){ // IPv4
            buf.writeByte(0x01);
            InetAddress address = Inet4Address.getByName(host);
            byte[] addr = address.getAddress();
            buf.writeBytes(addr);
        }else if(addressType == 0x03){ // 域名
            buf.writeByte(0x03);
            String address = IDN.toASCII(host);
            byte[] addr = address.getBytes(StandardCharsets.US_ASCII);
            log.info("address:{}, host:{}, addr:{}", address, host, Arrays.toString(addr));
            buf.writeByte(addr.length);
            buf.writeBytes(addr);
        }else{
            throw new IllegalArgumentException("不支持IPv6");
        }
        buf.writeShort(port);
        return buf;
    }
}
