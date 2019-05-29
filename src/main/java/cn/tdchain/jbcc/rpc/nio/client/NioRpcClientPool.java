/*
 * Copyright (c) 2017 Beijing Tiande Technology Co., Ltd.
 * All Rights Reserved.
 */
package cn.tdchain.jbcc.rpc.nio.client;

import cn.tdchain.jbcc.rpc.nio.handler.NioPoolableRpcClientFactory;
import cn.tdchain.jbcc.rpc.nio.handler.NioRpcClientPoolConfig;
import org.apache.commons.pool2.impl.GenericObjectPool;

public class NioRpcClientPool extends GenericObjectPool<NioRpcClient> {
    private NioRpcHeartClient rpcHeartClient;

    public NioRpcClientPool(NioPoolableRpcClientFactory factory, NioRpcClientPoolConfig config) {
        super(factory, config);
    }

    public boolean isAlived() {
        if (rpcHeartClient == null) {
            return false;
        }
        return rpcHeartClient.isActive();
    }

    public NioRpcHeartClient getRpcHeartClient() {
        return rpcHeartClient;
    }

    public void setRpcHeartClient(NioRpcHeartClient rpcHeartClient) {
        this.rpcHeartClient = rpcHeartClient;
    }

    @Override
    public void close() {
        super.close();
        rpcHeartClient.close();
    }
}
