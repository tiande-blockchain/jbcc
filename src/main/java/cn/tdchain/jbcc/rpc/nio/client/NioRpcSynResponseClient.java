/*
 * Copyright (c) 2017 Beijing Tiande Technology Co., Ltd.
 * All Rights Reserved.
 */
package cn.tdchain.jbcc.rpc.nio.client;

import cn.tdchain.jbcc.rpc.nio.handler.NioSynReponseClientHandler;
import io.netty.channel.Channel;
import io.netty.util.concurrent.DefaultProgressivePromise;
import io.netty.util.concurrent.Promise;

import java.util.concurrent.ExecutionException;

public class NioRpcSynResponseClient extends NioAbstractClient {

    public NioRpcSynResponseClient(NioRpcClient nioRpcClient) {
        super(nioRpcClient);
        if (nioRpcClient != null && nioRpcClient.isActive()) {
            this.nioRpcClient.getChannel().pipeline().addLast(new NioSynReponseClientHandler());
        }
    }

    /**
     * Send message and get response.
     *
     * @param message message
     * @return response message
     * @throws InterruptedException InterruptedException
     */
    public synchronized String sendAndReturn(String message) throws InterruptedException {
        return sendAndReturn(message, 1000);
    }

    /**
     * Send message and get response.
     *
     * @param message message
     * @return response message
     * @throws InterruptedException InterruptedException
     */
    public synchronized String sendAndReturn(String message, long timeMillis) throws InterruptedException {
        if (message == null) {
            return null;
        }
        Channel channel = this.nioRpcClient.getChannel();
        NioSynReponseClientHandler nioSynReponseClientHandler = channel.pipeline().get(NioSynReponseClientHandler.class);
        Promise<String> oldPromise = nioSynReponseClientHandler.getPromise();
        if (oldPromise != null && !oldPromise.isDone()) {
            try {
                oldPromise.await(timeMillis);
            } catch (InterruptedException e) {
                e.printStackTrace();
                oldPromise.tryFailure(e.getCause());
            }
        }
        Promise<String> promise = new DefaultProgressivePromise(channel.eventLoop());
        nioSynReponseClientHandler.setPromise(promise);
        channel.writeAndFlush(message);
        try {
            promise.await(timeMillis);
            if (promise.isSuccess()) {
                return promise.get();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }

}


