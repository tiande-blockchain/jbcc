/*
 * Copyright (c) 2017 Beijing Tiande Technology Co., Ltd.
 * All Rights Reserved.
 */
package cn.tdchain.jbcc.rpc.nio.handler;

import io.netty.channel.ChannelFactory;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

public class NioRpcClientPoolConfig extends GenericObjectPoolConfig {
    private ChannelFactory channelFactory;
    private String clientPublicKey = null;
    private String address = null;
    private String token = null;
    private int port;
    private long timeout = -1L;
    private String connectionId;

    public NioRpcClientPoolConfig(ChannelFactory channelFactory, String address, int port, long timeout, String token, String connectionId, String clientPublicKey) {
        super();
        this.channelFactory = channelFactory;
        this.address = address;
        this.port = port;
        this.timeout = timeout;
        this.token = token;
        this.connectionId = connectionId;
        this.clientPublicKey = clientPublicKey;
    }

    public ChannelFactory getChannelFactory() {
        return channelFactory;
    }

    public String getClientPublicKey() {
        return clientPublicKey;
    }

    public String getAddress() {
        return address;
    }

    public String getToken() {
        return token;
    }

    public int getPort() {
        return port;
    }

    public long getTimeout() {
        return timeout;
    }

    public String getConnectionId() {
        return connectionId;
    }

    public void setConnectionId(String connectionId) {
        this.connectionId = connectionId;
    }
}
