/*
 * Copyright (c) 2017 Beijing Tiande Technology Co., Ltd.
 * All Rights Reserved.
 */
package cn.tdchain.jbcc.rpc.nio.client;

import cn.tdchain.jbcc.net.Net;
import cn.tdchain.jbcc.net.nio.NioNet;
import cn.tdchain.jbcc.rpc.nio.handler.NioHeartClientHandler;
import cn.tdchain.jbcc.rpc.nio.handler.NioResponseClientHandler;
import io.netty.channel.Channel;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.TimeUnit;


public class NioRpcHeartClient extends NioAbstractClient {

    private NioHeartClientHandler nioHeartClientHandler;

    public NioRpcHeartClient(NioRpcClient nioRpcClient, NioNet.NioTask task) {
        super(nioRpcClient);
        nioHeartClientHandler = new NioHeartClientHandler(task);
        if (nioRpcClient != null && nioRpcClient.isActive()) {
            Channel channel = nioRpcClient.getChannel();
            channel.writeAndFlush(Net.heart).addListener(future -> {
                if (future.isSuccess()) {
                    ChannelPipeline pipeline = channel.pipeline();
                    pipeline.remove(LengthFieldBasedFrameDecoder.class);
                    pipeline.remove(LengthFieldPrepender.class);

                    pipeline.addLast(new IdleStateHandler(18, 5, 0, TimeUnit.SECONDS));
                    pipeline.addLast(nioHeartClientHandler);
                }
            });
        }
    }

    /**
     * Send message.
     *
     * @param msg message
     */
    public void send(String msg) {
        if (msg == null) {
            return;
        }
        this.nioRpcClient.send(msg);
    }

    public boolean isConnected() {
        if (nioHeartClientHandler == null) {
            return false;
        }
        return nioHeartClientHandler.isConnected();
    }

    public NioRpcClient getNioRpcClient() {
        return nioRpcClient;
    }

    public void setNioRpcClient(NioRpcClient nioRpcClient) {
        this.nioRpcClient = nioRpcClient;
    }
}

