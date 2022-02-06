package com.saum.netty.remote;

import com.saum.crypto.Crypto;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;

/**
 * @Author saum
 * @Description:
 */
@Slf4j
public class AddressHandler extends ChannelInboundHandlerAdapter {
    private final static int ADDR_TYPE_IPV4 = 1;
    private final static int ADDR_TYPE_HOST = 3;

    private final ByteBuf dataBuf = Unpooled.buffer();
    private final Crypto crypto;

    public AddressHandler(Crypto crypto){
        this.crypto = crypto;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("connected with {}", ctx.channel());
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = (ByteBuf) msg;
//        if(buf.readableBytes() <= 0){
//            return;
//        }
        byte[] bytes = ByteBufUtil.getBytes(buf);
        byte[] decrypted = crypto.decrypt(bytes);
        dataBuf.writeBytes(decrypted);
        String host;
        int port;
        int addressType = dataBuf.getUnsignedByte(0);
        if(addressType == ADDR_TYPE_IPV4){
            if(dataBuf.readableBytes() < 7){
                return;
            }
            // addrType(1) + ipv4(4) + port(2)
            dataBuf.readUnsignedByte();
            dataBuf.readByte();
            byte[] ipBytes = new byte[4];
            dataBuf.readBytes(ipBytes);
            host = InetAddress.getByAddress(ipBytes).toString().substring(1);
            port = dataBuf.readShort();
            log.info("ip:{}:{}", InetAddress.getByAddress(ipBytes).toString(), port);
        }else if(addressType == ADDR_TYPE_HOST){
            int hostLen = dataBuf.getUnsignedByte(1);
            // addrType(1) + hostlen(1) + host + port(2)
            if(dataBuf.readableBytes() < hostLen + 4){
                return;
            }
            dataBuf.readUnsignedByte();
            dataBuf.readUnsignedByte();
            byte[] hostBytes = new byte[hostLen];
            dataBuf.readBytes(hostBytes);
            host = new String(hostBytes);
            port = dataBuf.readShort();
            log.info("host:{}:{}", host, port);
        }else{
            throw new IllegalStateException("unknown address type: " + addressType);
        }
        ctx.channel().pipeline().addLast(new RemoteConnectHandler(host, port, ctx, crypto, dataBuf));
        ctx.channel().pipeline().remove(this);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.info("disconnected with {}", ctx.channel());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
    }
}
