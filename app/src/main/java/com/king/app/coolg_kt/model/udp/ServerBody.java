package com.king.app.coolg_kt.model.udp;

/**
 * Desc:本机端口与身份标识，通过UDP广播
 *
 * @author：Jing Yang
 * @date: 2020/4/28 18:10
 */
public class ServerBody {

    private String identity;
    private int port;
    private String extraUrl;
    private String serverName;

    private String ip;

    /**
     * 扩展字段
     */
    private boolean isOnline;
    private boolean isManuel;

    public String getIdentity() {
        return identity;
    }

    public void setIdentity(String identity) {
        this.identity = identity;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getExtraUrl() {
        return extraUrl;
    }

    public void setExtraUrl(String extraUrl) {
        this.extraUrl = extraUrl;
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public boolean isOnline() {
        return isOnline;
    }

    public void setOnline(boolean online) {
        isOnline = online;
    }

    public boolean isManuel() {
        return isManuel;
    }

    public void setManuel(boolean manuel) {
        isManuel = manuel;
    }
}
