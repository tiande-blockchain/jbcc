/*
 * Copyright (c) 2017 Beijing Tiande Technology Co., Ltd.
 * All Rights Reserved.
 */
package cn.tdchain.jbcc;

import org.apache.commons.lang3.StringUtils;

/**
 * function：
 * datetime：2019-03-27 10:03
 * author：warne
 */
public class ConnectionBuilder {
    private Connection connection;

    public Connection getConnection() {
        if (connection == null) {
            //校验
            verify();
            connection = new Connection(iptables, port, token, timeout, ksPath, ksPasswd);
        }
        return connection;
    }

    public static ConnectionBuilder instance() {
        return new ConnectionBuilder();
    }

    public ConnectionBuilder iptables(String[] iptables) {
        this.iptables = iptables;
        return this;
    }

    public ConnectionBuilder port(int port) {
        this.port = port;
        return this;
    }

    public ConnectionBuilder token(String token) {
        this.token = token;
        return this;
    }

    public ConnectionBuilder timeout(long timeout) {
        this.timeout = timeout;
        return this;
    }

    public ConnectionBuilder ksPath(String ksPath) {
        this.ksPath = ksPath;
        return this;
    }


    public ConnectionBuilder ksPasswd(String ksPasswd) {
        this.ksPasswd = ksPasswd;
        return this;
    }

    public void verify() {
        if (iptables == null || iptables.length == 0) {
            throw new RuntimeException("iptables connfig error, please check iptables ");
        }
        if (port < 1025 || port > 65534) {
            throw new RuntimeException("port error. [1025~65534] ");
        }

        if (StringUtils.isBlank(token)) {
            throw new RuntimeException("token is empty ");
        }
        if (StringUtils.isBlank(ksPath))
            throw new RuntimeException("keystore path is empty ");

        if (StringUtils.isBlank(ksPasswd)) {
            throw new RuntimeException("keystore passwd is empty ");
        }
    }

    private String[] iptables;
    private int port;
    private String token;
    private long timeout;
    private String ksPath;
    private String ksPasswd;


}
