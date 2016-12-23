package cn.net.communion.sync.entity;

public class Node {
    private String ip = "127.0.0.1";
    private int port = 9300;

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
