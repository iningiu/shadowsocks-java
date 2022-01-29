package com.saum.socket.socks;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;

/**
 * @Author saum
 * @Description:
 */
@Slf4j
@Data
public class SocksConnectionRequest {

    private int version;
    private int cmd;
    private int rsv;
    private int atyp;
    private String addr;
    private int port;

    // 构造器私有，data必须先校验
    private SocksConnectionRequest(byte[] data){
        this.version = data[0];
        this.cmd = data[1];
        this.rsv = data[2];
        this.atyp = data[3];

        if(atyp == 0x01){ // IPv4
            this.addr = bytesToIP(Arrays.copyOfRange(data, 4, data.length - 2), '.');
        }else if(atyp == 0x03){ // 域名
            this.addr = bytesToDomain(Arrays.copyOfRange(data, 5, data.length - 2));
        }else if(atyp == 0x04){ // IPv6
            this.addr = bytesToIP(Arrays.copyOfRange(data, 4, data.length - 2), ':');
        }

//        this.port = Integer.parseInt(new String(data, data.length - 2, 2), 16);
        this.port = (data[data.length - 2] & 0xFF) * 256 + (data[data.length - 1] & 0xFF);
    }

    private static boolean validate(byte[] data){
        if(data.length < 7 || data[0] != 0x05 || data[2] != 0x00) return false;
        return data[3] == 0x01 || data[3] == 0x03 || data[3] == 0x04;
    }

    public static SocksConnectionRequest fromBytes(byte[] data){
        if(validate(data)){
            return new SocksConnectionRequest(data);
        }else{
            return null;
        }
    }

    private String bytesToDomain(byte[] addrBytes){
        StringBuilder sb = new StringBuilder();
        for(byte addrByte : addrBytes){
            sb.append((char) addrByte);
        }
        return sb.toString();
    }

    private String bytesToIP(byte[] addrBytes, char ch){
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < addrBytes.length; i++) {
            if(i < addrBytes.length - 1){
                sb.append(addrBytes[i]).append(ch);
            }
        }
        return sb.toString();
    }
}
