package com.saum.netty;

import com.saum.config.Config;
import com.saum.config.ConfigLoader;
import com.saum.socket.SocketMain;

import java.util.Objects;

/**
 * @Author saum
 * @Description:
 */
public class NettyMain {
    public static void main(String[] args) {
        String configPath = Objects.requireNonNull(SocketMain.class.getClassLoader().getResource("config.json")).getPath();
        Config config = ConfigLoader.loadConfig(configPath);

        Thread t1 = new Thread(() -> {
            NettyLocal nettyLocal = new NettyLocal(config);
            nettyLocal.start();
        });

        Thread t2 = new Thread(() -> {
            NettyServer nettyServer = new NettyServer(config);
            nettyServer.start();
        });

        t1.start();
        t2.start();
    }
}
