/*
 * Copyright (c) 2017 Beijing Tiande Technology Co., Ltd.
 * All Rights Reserved.
 */
package cn.tdchain.jbcc.rpc.nio.handler;

import cn.tdchain.cipher.utils.StringUtils;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.concurrent.Promise;

/**
 * Synchronize NioAbstractClient Handler.
 *
 * @author murong.H 2018-08-17
 * @version 1.0
 */
public class NioSynReponseClientHandler extends ChannelInboundHandlerAdapter {
    private Promise<String> promise;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        String message = (String) msg;
        if (StringUtils.isBlank(message)) {
            return;
        }
        this.promise.trySuccess(message);
    }

    public Promise<String> getPromise() {
        return promise;
    }

    public void setPromise(Promise<String> promise) {
        this.promise = promise;
    }
}
