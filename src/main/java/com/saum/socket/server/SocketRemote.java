package com.saum.socket.server;

import com.saum.config.Config;
import com.saum.crypto.Crypto;
import com.saum.crypto.CryptoFactory;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @Author saum
 * @Description:
 */
@Slf4j
public class SocketRemote {

    public static Crypto crypto;
    private Config config;

    public SocketRemote(Config config){
        this.config = config;
        this.crypto = CryptoFactory.create(config.getMethod(), config.getPassword());
    }

    public void listen(){
        log.info("rmeote server start listening...");
        try {
            ServerSocket serverSocket = new ServerSocket(config.getServerPort());
            while(true){
                Socket socket = serverSocket.accept();
                new SocketRemoteServerThread(socket).start();
            }
        } catch (IOException e) {
            log.error("远程代理服务端出错：{}", e);
        }
    }
}
