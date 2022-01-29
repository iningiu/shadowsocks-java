package com.saum.socket.server;

import com.saum.socket.socks.SocksConnectionRequest;
import com.saum.socket.socks.SocksConnectionResponse;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Arrays;

/**
 * @Author saum
 * @Description:
 */
@Slf4j
public class SocketRemoteServerThread extends Thread {

    // 远程代理和本地代理之间的socket
    private Socket proxySocket;
    private InputStream proxyIn;
    private OutputStream proxyOut;

    // 实际的请求地址
    private String remoteAddr;
    // 请求端口
    private int remotePort;

    private final static int SOCKS_CONNECTION_MAX = 512; //1 + 1 + 1 + 1 + (1 + 255) + 2 = 262

    public SocketRemoteServerThread(Socket socket) throws IOException {
        this.proxySocket = socket;
        this.proxyIn = proxySocket.getInputStream();
        this.proxyOut = proxySocket.getOutputStream();
    }

    @Override
    public void run() {
        try {
            proxySocket.setSoTimeout(1000 * 60 * 10);
            proxySocket.setKeepAlive(true);
            // socks5握手
            if(!socks5HandShake(proxyIn, proxyOut)){
                log.error("socks5HandShake failed!");
                proxySocket.close();
                return;
            }

            if(!socks5Connection(proxyIn, proxyOut)){
                log.error("socks5Connection failed!");
                proxySocket.close();
                return;
            }

        } catch (IOException e) {
            log.error("", e);
        }
    }

    // 握手阶段
    private boolean socks5HandShake(InputStream in, OutputStream out){
        byte[] data = decryptAllBytes(in);
        if(data == null || data[0] != 0x05){
            return false;
        }

        byte[] response = new byte[2];
        response[0] = 0x05;
        return encryptAllBytes(out, response);
    }

    // 建立连接
    private boolean socks5Connection(InputStream in, OutputStream out){
        byte[] data = decryptAllBytes(in);
        if(data == null){
            return false;
        }

        SocksConnectionRequest request = SocksConnectionRequest.fromBytes(data);
        if(request == null){
            return false;
        }

        remoteAddr = request.getAddr();
        remotePort = request.getPort();
        log.info("proxy to remote: " + remoteAddr + ":" + remotePort);

        byte[] response = SocksConnectionResponse.responseByes();
        return encryptAllBytes(proxyOut, response);

    }

    // 远程代理转发浏览器的访问请求
    private void startForward(){
        // 这是远程代理和目标服务器之间的socket
        Socket remoteSocket = null;
        OutputStream remoteOut = null;
        InputStream remoteIn = null;
        try {
            remoteSocket = new Socket(remoteAddr, remotePort);
            remoteOut = remoteSocket.getOutputStream();
            remoteIn = remoteSocket.getInputStream();

            remoteSocket.setSoTimeout(1000 * 60 * 10);
            remoteSocket.setKeepAlive(true);

            StreamForwardThread decryptThread = new StreamForwardThread(proxyIn, remoteOut, SocketRemote.crypto, false);
            StreamForwardThread encryptThread = new StreamForwardThread(remoteIn, proxyOut, SocketRemote.crypto, true);

            decryptThread.start();
            encryptThread.start();

            decryptThread.join();
            encryptThread.join();
        } catch (IOException | InterruptedException e) {
            log.error("", e);
        }

        // 关闭流
        try {
            remoteIn.close();
            remoteOut.close();
            remoteSocket.close();

            proxyIn.close();
            proxyOut.close();
            proxySocket.close();
        } catch (IOException e) {
            log.error("", e);
        }
    }

    private byte[] decryptAllBytes(InputStream in){
        try {
            byte[] buffer = new byte[SOCKS_CONNECTION_MAX + 10];
            int len = in.read(buffer);
            SocketRemote.crypto.decrypt(buffer);
            if(len == buffer.length){
                log.warn("reach buffer limit.");
            }
            return Arrays.copyOf(buffer, len);
        } catch (IOException e) {
            log.error("", e);
            return null;
        }
    }

    private boolean encryptAllBytes(OutputStream out, byte[] rawBuffer){
        try {
            byte[] encrypt = SocketRemote.crypto.encrypt(rawBuffer);
            out.write(encrypt);
            out.flush();
            return true;
        } catch (IOException e) {
            log.error("", e);
            return false;
        }
    }
}
