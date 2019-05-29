/*
 * Copyright (c) 2017 Beijing Tiande Technology Co., Ltd.
 * All Rights Reserved.
 */
package cn.tdchain.jbcc.rpc.nio.handler;

import cn.tdchain.cipher.rsa.AesUtil;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.concurrent.Promise;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class NioAuthChannelHandler extends ChannelInboundHandlerAdapter {
    private Map<NioHandshakerType, String> handshakerMap = new HashMap<>();
    private Promise<Map<NioHandshakerType, String>> promise = null;
    private String token;
    private String publicKey;
    private String connectionId;

    public NioAuthChannelHandler(Promise<Map<NioHandshakerType, String>> promise, String token, String connectionId, String publicKey) {
        this.promise = promise;
        this.token = token;
        this.publicKey = publicKey;
        this.connectionId = connectionId;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        String authSequence = (String) msg;
        if (!handshakerMap.containsKey(NioHandshakerType.HAND_2)) {
            String data = handshakerMap.get(NioHandshakerType.AUTH);
            String decode = authSequence.split(";")[0];//明文
            String encode = authSequence.split(";")[1]; //对方密文
            if (data.equals(decode)) {
                data = AesUtil.decrypt(encode, token);
                if (data != null) {
                    handshakerMap.put(NioHandshakerType.HAND_2, NioHandshakerType.HAND_2.name());
                    ctx.writeAndFlush(data);
                }
            }
            return;
        }
        if (!handshakerMap.containsKey(NioHandshakerType.HAND_3)) {
            // 第二次握手逻辑,接收服务端公钥
            handshakerMap.put(NioHandshakerType.SERVER_PUBLIC_KEY, authSequence);
            handshakerMap.put(NioHandshakerType.HAND_3, NioHandshakerType.HAND_3.name());
            // 发送自己的公钥和connectId给服务端
            String keyAndConnectId = publicKey + ";" + connectionId;
            ChannelFuture future = ctx.writeAndFlush(keyAndConnectId);
            future.addListener(f -> {
                if (f.isSuccess()) {
                    promise.trySuccess(handshakerMap);
                } else {
                    promise.tryFailure(new NioRpcClientException("auth handshaker failed!"));
                }
            });

        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        String uuid = UUID.randomUUID().toString();
        String encode = AesUtil.encrypt(uuid, this.token);
        handshakerMap.put(NioHandshakerType.AUTH, uuid);
        ctx.writeAndFlush(encode);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        ctx.close();
        cause.printStackTrace();
    }

}
