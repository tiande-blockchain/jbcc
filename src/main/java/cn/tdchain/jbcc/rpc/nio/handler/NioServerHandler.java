/*
 * Copyright (c) 2017 Beijing Tiande Technology Co., Ltd.
 * All Rights Reserved.
 */
package cn.tdchain.jbcc.rpc.nio.handler;

import cn.tdchain.jbcc.Connection;
import cn.tdchain.jbcc.net.ConnectionCount;
import cn.tdchain.jbcc.net.Net;
import cn.tdchain.jbcc.rpc.RPCMessage;
import cn.tdchain.jbcc.rpc.nio.server.NioRpcServer;

import com.alibaba.fastjson.JSONObject;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * TCP message handler.
 *
 * @author murong 2018-08-03
 * @version 1.0
 */
@ChannelHandler.Sharable
public class NioServerHandler extends ChannelInboundHandlerAdapter {
//    protected static Logger log = LogManager.getLogger("TDBC");

    private NioRpcServer nioRpcServer;

    public NioServerHandler(NioRpcServer nioRpcServer) {
        this.nioRpcServer = nioRpcServer;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object rmsg) throws Exception {
        String str = (String) rmsg;
        if (Net.heart.equals(str)) {
            ConnectionCount.newInstance().handleHeart(ctx.channel());
            return;
        }
        RPCMessage msg = JSONObject.parseObject(str, RPCMessage.class);
        String connectionId = msg.getSender();
        if (RPCMessage.TargetType.BATCH_RESPHONE == msg.getTargetType()) {
            ConnectionCount.newInstance().handleSocket(connectionId, ctx.channel());
        } else {
            // 一般是request的请求
            String result = nioRpcServer.getHandler().handler(msg, connectionId);
            if (result != null) {
                ctx.writeAndFlush(result);
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
            throws Exception {
        cause.printStackTrace();
//        log.error("a request error: {}", cause.getMessage());
    }

    public NioRpcServer getNioRpcServer() {
        return nioRpcServer;
    }

    public void setNioRpcServer(NioRpcServer nioRpcServer) {
        this.nioRpcServer = nioRpcServer;
    }

}
