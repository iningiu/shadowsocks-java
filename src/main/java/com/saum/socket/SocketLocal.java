package com.saum.socket;

import com.saum.config.Config;
import com.saum.crypto.Crypto;
import com.saum.crypto.CryptoFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @Author saum
 * @Description:
 */
public class SocketLocal {
    private static Logger logger = LoggerFactory.getLogger(SocketLocal.class);

    private Config config;
    public static Crypto crypto;
    public SocketLocal(Config config){
        this.config = config;
        this.crypto = CryptoFactory.create(config.getMethod(), config.getPassword());
    }

    public void listen(){
        try {
            // 本地代理服务端
            ServerSocket localServerSocket = new ServerSocket(config.getLocalPort());
            logger.info("local port start listening...");
            while(true){
                Socket socket = localServerSocket.accept();
                logger.info("receive proxy request: 127.0.0.1:" + socket.getPort());
                // 每当浏览器发起一次访问请求时，本地代理和远程代理都要重新建立连接
                new SocketLocalServerThread(socket, config.getServer(), config.getServerPort()).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
