package com.saum.config;

import com.google.gson.annotations.SerializedName;

/**
 * @Author saum
 * @Description:
 */
public class Config {

    @SerializedName("server")
    private String server;

    @SerializedName("server_port")
    private Integer serverPort;

    @SerializedName("local")
    private String local;

    @SerializedName("local_port")
    private Integer localPort;

    @SerializedName("method")
    private String method;

    @SerializedName("password")
    private String password;

    @SerializedName("timeout")
    private Integer timeout;

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public Integer getServerPort() {
        return serverPort;
    }

    public void setServerPort(Integer serverPort) {
        this.serverPort = serverPort;
    }

    public String getLocal() {
        return local;
    }

    public void setLocal(String local) {
        this.local = local;
    }

    public Integer getLocalPort() {
        return localPort;
    }

    public void setLocalPort(Integer localPort) {
        this.localPort = localPort;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Integer getTimeout() {
        return timeout;
    }

    public void setTimeout(Integer timeout) {
        this.timeout = timeout;
    }
}
