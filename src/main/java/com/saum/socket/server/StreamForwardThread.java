package com.saum.socket.server;

import com.saum.crypto.Crypto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @Author saum
 * @Description:
 */
public class StreamForwardThread extends Thread {

    private final Logger logger = LoggerFactory.getLogger(StreamForwardThread.class);

    private static final int BUFFER_SIZE = 1024 * 256; // 256k
    private final InputStream in;
    private final OutputStream out;
    private final Crypto crypto;
    private final boolean isEncrypt; // 是否加密，true加密，false解密
    private final byte[] buffer;

    /**
     * in ---decrypt/encrypted----> out
     */
    public StreamForwardThread(InputStream in, OutputStream out, Crypto crypto, boolean isEncrypt){
        this.in = in;
        this.out = out;
        this.crypto = crypto;
        this.isEncrypt = isEncrypt;
        buffer = new byte[BUFFER_SIZE];
    }

    @Override
    public void run() {
        try {
            int len;
            if(isEncrypt){
                while ((len = in.read(buffer)) != -1) {
                    if (len == 0) continue;
                    crypto.encrypt(buffer);
                    out.write(buffer, 0, len);
                }
            }else{
                while ((len = in.read(buffer)) != -1) {
                    if(len == 0) continue;
                    crypto.decrypt(buffer);
                    out.write(buffer, 0, len);
                }
            }
            out.flush();
        } catch (IOException e) {
            logger.error("isEncrypt:{}, 代理服务端加解密时出错：{}", isEncrypt, e.getMessage(), e);
        }
    }
}
