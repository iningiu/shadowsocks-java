package com.saum.socket;

import com.saum.config.Config;
import com.saum.config.ConfigLoader;
import com.saum.socket.server.SocketLocal;
import com.saum.socket.server.SocketRemote;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

/**
 * @Author saum
 * @Description:
 */
@Slf4j
public class SocketMain {

    public static void main(String[] args) {
        String configPath = Objects.requireNonNull(SocketMain.class.getClassLoader().getResource("config.json")).getPath();
        Config config = ConfigLoader.loadConfig(configPath);

        Thread t1 = new Thread(() -> {
            SocketLocal socketLocal = new SocketLocal(config);
            socketLocal.listen();
        });

        Thread t2 = new Thread(() -> {
            SocketRemote socketRemote = new SocketRemote(config);
            socketRemote.listen();
        });

        t1.start();
        t2.start();

    }
}
