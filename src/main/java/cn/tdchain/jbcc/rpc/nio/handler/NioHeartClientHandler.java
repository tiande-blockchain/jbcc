/*
 * Copyright (c) 2017 Beijing Tiande Technology Co., Ltd.
 * All Rights Reserved.
 */
package cn.tdchain.jbcc.rpc.nio.handler;

import cn.tdchain.jbcc.net.Net;
import cn.tdchain.jbcc.net.nio.NioNet;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

/**
 * TCP message handler.
 *
 * @author murong 2018-08-03
 * @version 1.0
 */
public class NioHeartClientHandler extends ChannelInboundHandlerAdapter {
    private NioNet.NioTask task;
    private boolean isConnected = true;

    public NioHeartClientHandler(NioNet.NioTask task) {
        this.task = task;
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        IdleStateEvent idle = (IdleStateEvent) evt;
        if (idle.state() == IdleState.WRITER_IDLE) {
            // 每隔5s发送一次
            ctx.writeAndFlush(Net.heart);
        } else if (idle.state() == IdleState.READER_IDLE) {
            isConnected = false;
            task.stop();
            ctx.close();
        }
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) {
        isConnected = false;
        task.stop();
    }


    public boolean isConnected() {
        return isConnected;
    }
}
