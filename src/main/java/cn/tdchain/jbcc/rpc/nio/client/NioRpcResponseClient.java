/*
 * Copyright (c) 2017 Beijing Tiande Technology Co., Ltd.
 * All Rights Reserved.
 */
package cn.tdchain.jbcc.rpc.nio.client;

import cn.tdchain.jbcc.rpc.nio.handler.NioResponseClientHandler;


public class NioRpcResponseClient extends NioAbstractClient {


    public NioRpcResponseClient(NioRpcClient nioRpcClient) {
        super(nioRpcClient);
        if (nioRpcClient != null && nioRpcClient.isActive()) {
            this.nioRpcClient.getChannel().pipeline().addLast(new NioResponseClientHandler());
        }
    }

    /**
     * Send message.
     * @param msg message
     */
    public void send(String msg) {
        if (msg == null) {
            return;
        }
        this.nioRpcClient.send(msg);
    }

    public NioRpcClient getNioRpcClient() {
        return nioRpcClient;
    }

    public void setNioRpcClient(NioRpcClient nioRpcClient) {
        this.nioRpcClient = nioRpcClient;
    }
}

