/*
 * Copyright (c) 2017 Beijing Tiande Technology Co., Ltd.
 * All Rights Reserved.
 */
package cn.tdchain.jbcc.rpc.nio.client;


public abstract class NioAbstractClient {
    protected NioRpcClient nioRpcClient;


    public NioAbstractClient(NioRpcClient nioRpcClient) {
        this.nioRpcClient = nioRpcClient;
    }

    public void close() {
        nioRpcClient.close();
    }

    public boolean isActive() {
        return nioRpcClient.isActive();
    }


    public NioRpcClient getNioRpcClient() {
        return nioRpcClient;
    }

    public void setNioRpcClient(NioRpcClient nioRpcClient) {
        this.nioRpcClient = nioRpcClient;
    }
}

