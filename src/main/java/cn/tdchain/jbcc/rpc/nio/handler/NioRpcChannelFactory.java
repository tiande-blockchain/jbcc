/*
 * Copyright (c) 2017 Beijing Tiande Technology Co., Ltd.
 * All Rights Reserved.
 */
package cn.tdchain.jbcc.rpc.nio.handler;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFactory;

public class NioRpcChannelFactory implements ChannelFactory {

    private Bootstrap bootstrap;

    public NioRpcChannelFactory(Bootstrap bootstrap) {
        this.bootstrap = bootstrap;
    }

    @Override
    public Channel newChannel() {
        return this.bootstrap.register().syncUninterruptibly().channel();
    }
}
