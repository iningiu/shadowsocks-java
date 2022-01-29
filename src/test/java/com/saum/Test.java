package com.saum;

import java.io.*;
import java.net.Socket;

/**
 * @Author saum
 * @Description:
 */
public class Test {

    @org.junit.Test
    public void test1() throws IOException {
        // https://blog.csdn.net/xyls12345/article/details/25778231
        Socket socket = new Socket("127.0.0.1", 443);
        InputStream in = socket.getInputStream();
        OutputStream out = socket.getOutputStream();
//        in.close();
        out.close();
        System.out.println(socket.isClosed());
    }

    @org.junit.Test
    public void test2() throws IOException {
        // https://www.i4k.xyz/article/bboxhe/45970997
//        final String CONTENT = "GET /  HTTP/1.1\r\n Host: www.baidu.com:80\r\n" ;

        Socket socket = new Socket("www.baidu.com", 80);
        InputStream in = socket.getInputStream();
        BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
//        OutputStream out = socket.getOutputStream();
//        out.write(CONTENT.getBytes());

        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        out.println("GET / HTTP/1.1");
        out.println("Host: www.baidu.com:80");
        out.println("\r\n");

        String line = null;
        while((line = br.readLine()) != null){
            System.out.println(line);
        }
    }
}
