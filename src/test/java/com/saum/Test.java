package com.saum;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * @Author saum
 * @Description:
 */
public class Test {
    public static void main(String[] args) throws IOException {
        // https://blog.csdn.net/xyls12345/article/details/25778231
        Socket socket = new Socket("127.0.0.1", 443);
        InputStream in = socket.getInputStream();
        OutputStream out = socket.getOutputStream();
//        in.close();
        out.close();
        System.out.println(socket.isClosed());
    }
}
