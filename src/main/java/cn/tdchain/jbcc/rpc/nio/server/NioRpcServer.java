/*
 * Copyright (c) 2017 Beijing Tiande Technology Co., Ltd.
 * All Rights Reserved.
 */
package cn.tdchain.jbcc.rpc.nio.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.CharsetUtil;

import java.net.InetSocketAddress;

import cn.tdchain.cipher.Key;
import cn.tdchain.jbcc.rpc.MessageHandler;
import cn.tdchain.jbcc.rpc.RpcServer;
import cn.tdchain.jbcc.rpc.nio.handler.NioAuthServerHandler;


/**
 * rpc server.
 *
 * @author murong 2017-12-13
 * @version 2.0
 */
public class NioRpcServer implements RpcServer {

    private String token;

    private int port;

    private MessageHandler handler;

    private Key key = null;

    private Channel channel;
    

    /**
     * Constructor.
     * @param port
     * @param handler
     * @param token
     * @param key
     */
    public NioRpcServer(int port, MessageHandler handler, String token, Key key) {
        this.port = port;
        this.handler = handler;
        this.token = token;
        this.key = key;
    }

    /**
     * Start server.
     */
    public void startServer() {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        StringDecoder stringDecoder = new StringDecoder(CharsetUtil.UTF_8);
        StringEncoder stringEncoder = new StringEncoder(CharsetUtil.UTF_8);
        ServerBootstrap b = new ServerBootstrap();
        b.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .localAddress(new InetSocketAddress(port))
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel)
                            throws Exception {
                        socketChannel.config().setAllocator(UnpooledByteBufAllocator.DEFAULT);
                        socketChannel.pipeline()
                                .addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, 0, 4))
                                .addLast(new LengthFieldPrepender(4))
                                .addLast(stringDecoder)
                                .addLast(stringEncoder)
                                .addLast(new NioAuthServerHandler(NioRpcServer.this));
                    }
                });
        ChannelFuture future = null;
        try {
            future = b.bind().sync();
        } catch (InterruptedException e) {
        }
        channel = future.channel();
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public MessageHandler getHandler() {
        return handler;
    }

    public void setHandler(MessageHandler handler) {
        this.handler = handler;
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

	public Key getKey() {
		return key;
	}

}

