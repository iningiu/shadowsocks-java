package com.saum.socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;

/**
 * @Author saum
 * @Description:
 */
public class SocketLocalServerThread extends Thread {

    private final Logger logger = LoggerFactory.getLogger(SocketLocalServerThread.class);

    // 浏览器和本地代理之间的socket
    private Socket localSocket;

    // 本地代理和远程代理之间的socket
    private Socket remoteSocket;

    // 浏览器和本地代理之间，本地代理的输入流即浏览器的访问请求
    private final InputStream localIn;
    // 浏览器和本地代理之间，本地代理的输出流即解密后的响应数据
    private final OutputStream localOut;

    // 本地代理和远程代理之间，本地代理接收来自远程代理的待解密的响应数据
    private final InputStream remoteIn;
    // 本地代理和远程代理之间，本地代理发送给远程代理加密后的访问请求
    private final OutputStream remoteOut;

    public SocketLocalServerThread(Socket localSocket, String remoteAddr, int remotePort) throws IOException {
        this.localSocket = localSocket;
        try {
            this.remoteSocket = new Socket(remoteAddr, remotePort);
        } catch (IOException e) {
            logger.error("远程代理的IP[{}]或端口[{}]错误", remoteAddr, remotePort);
            this.localSocket.close();
            this.remoteSocket.close();
        }

        this.localIn = localSocket.getInputStream();
        this.localOut = localSocket.getOutputStream();
        this.remoteIn = remoteSocket.getInputStream();
        this.remoteOut = remoteSocket.getOutputStream();
    }

    @Override
    public void run() {
        try {
            localSocket.setSoTimeout(1000 * 60 * 10);
            localSocket.setKeepAlive(true);
            remoteSocket.setSoTimeout(1000 * 60 * 60);
            remoteSocket.setKeepAlive(true);

            StreamForwardThread encryptThread = new StreamForwardThread(localIn, remoteOut, SocketLocal.crypto, true);
            StreamForwardThread decryptThread = new StreamForwardThread(remoteIn, localOut, SocketLocal.crypto, false);

            encryptThread.start();
            decryptThread.start();

            // 等待加解密线程运行完毕
            encryptThread.join();
            decryptThread.join();
        } catch (SocketException | InterruptedException e) {
            logger.error("本地代理服务端出错：{}", e);
        }

        try {
            this.localIn.close();
            this.localOut.close();
            this.remoteIn.close();
            this.remoteOut.close();
        } catch (IOException e) {
            logger.error("本地代理服务端出错：{}", e);
        }
    }
}
