/*
 * Copyright (c) 2017 Beijing Tiande Technology Co., Ltd.
 * All Rights Reserved.
 */
package cn.tdchain.jbcc.rpc.nio.handler;

import cn.tdchain.jbcc.net.Net;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * TCP message handler.
 *
 * @author murong 2018-08-03
 * @version 1.0
 */
public class NioHeartServerHandler extends ChannelInboundHandlerAdapter {

    private long nearTime = System.currentTimeMillis();
    private boolean active = true;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        nearTime = System.currentTimeMillis();
        ctx.writeAndFlush(Net.heart);
    }

    public boolean isTimeOut() {
        // 如果连接断开,则肯定超时
        if (active == false) {
            return true;
        }
        long l = System.currentTimeMillis() - nearTime;
        if (l > 20000) {
            return true;
        }
        return false;
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext var1) throws Exception {
        this.active = false;
    }
}
