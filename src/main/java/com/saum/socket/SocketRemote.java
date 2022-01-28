package com.saum.socket;

import com.saum.crypto.Crypto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @Author saum
 * @Description:
 */
public class SocketRemote {
    private static Logger logger = LoggerFactory.getLogger(SocketRemote.class);

    public static Crypto crypto;
    private final int port;
    public SocketRemote(int port){
        this.port = port;
    }

    public void listen(){
        logger.info("rmeote server start listening...");
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            while(true){
                Socket socket = serverSocket.accept();

            }
        } catch (IOException e) {
            logger.error("{}", e);
        }
    }
}
