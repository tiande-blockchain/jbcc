/*
 * Copyright (c) 2017 Beijing Tiande Technology Co., Ltd.
 * All Rights Reserved.
 */
package cn.tdchain.jbcc.rpc.nio.handler;

import cn.tdchain.cipher.rsa.AesUtil;
import cn.tdchain.cipher.rsa.RsaUtil;
import cn.tdchain.jbcc.net.ConnectionCount;
import cn.tdchain.jbcc.rpc.BlackList;
import cn.tdchain.jbcc.rpc.io.eclc.EclcException;
import cn.tdchain.jbcc.rpc.nio.handler.NioHandshakerType;
import cn.tdchain.jbcc.rpc.nio.server.NioRpcServer;
import cn.tdchain.tdmsp.Msp;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;

import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * TCP message handler.
 *
 * @author murong 2018-08-03
 * @version 1.0
 */
public class NioAuthServerHandler extends ChannelInboundHandlerAdapter {

    private NioRpcServer nioRpcServer;
    private Map<NioHandshakerType, String> temp = new HashMap<>();

    public NioAuthServerHandler(NioRpcServer nioRpcServer) {
        this.nioRpcServer = nioRpcServer;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        String str = (String) msg;
        if (str == null || str.length() == 0) {
            ctx.close();
        }
        if (!temp.containsKey(NioHandshakerType.HAND_1)) {
            String c_data = AesUtil.decrypt(str, nioRpcServer.getToken());
            String s_data = UUID.randomUUID().toString();
            String s_r_data = c_data + ";" + AesUtil.encrypt(s_data, nioRpcServer.getToken());
            temp.put(NioHandshakerType.AUTH, s_data);
            temp.put(NioHandshakerType.HAND_1, NioHandshakerType.HAND_1.name());
            ctx.writeAndFlush(s_r_data);
            return;
        }
        if (!temp.containsKey(NioHandshakerType.HAND_2)) {
            String s_data = temp.get(NioHandshakerType.AUTH);
            if (s_data.equals(str)) {
                temp.put(NioHandshakerType.HAND_2, NioHandshakerType.HAND_2.name());
                ctx.writeAndFlush(nioRpcServer.getKey().getPublicKey());
            }
            return;
        }
        if (!temp.containsKey(NioHandshakerType.HAND_3)) {
            //接收客户端发送的证书，验证证书是否是合法证书，如果是则提取公钥，否则端口连接。
            String[] splits = str.split(";");
            String clientCertBase64Str = splits[0];
            String connectId = splits[1];
            X509Certificate clientCert = Msp.base64StringToCert(clientCertBase64Str);
            boolean leg = Msp.validateCert(Msp.base64StringToCert(nioRpcServer.getKey().getRootCertBase64String()), clientCert);//验证客户端证书是否合法?
            if (leg) {
                //合法证书,提取公钥。
                String clientPubliKey = RsaUtil.getPublicKey(clientCert.getPublicKey());
                //允许建立长连接
                temp.put(NioHandshakerType.CLIENT_PUBLIC_KEY, clientPubliKey);
                temp.put(NioHandshakerType.HAND_3, NioHandshakerType.HAND_3.name());
                String ogName = Msp.getOrganizationName(clientCert);
                if (ConnectionCount.newInstance().checkSingle(connectId, clientPubliKey)) {
                    ConnectionCount.newInstance().handleConnection(connectId, clientPubliKey, ogName, ctx.channel());
                    Attribute<String> attr = ctx.channel().attr(AttributeKey.valueOf(NioHandshakerType.CONNECTIONID.name()));
                    attr.set(connectId);
                } else {
                    throw new EclcException("nio shake  failed! Illegal certificate. params");
                }
            } else {
                //非法证书
                throw new EclcException("noi shake  failed! Illegal certificate.");
            }
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx)
            throws Exception {
        if (temp.containsKey(NioHandshakerType.CLIENT_PUBLIC_KEY)) {
            // 设置channel 属性
            Attribute<String> attr = ctx.channel().attr(AttributeKey.valueOf(NioHandshakerType.CLIENT_PUBLIC_KEY.name()));
            attr.set(temp.get(NioHandshakerType.CLIENT_PUBLIC_KEY));
            ctx.channel().pipeline().addLast(new NioServerHandler(nioRpcServer));
            ctx.channel().pipeline().remove(NioAuthServerHandler.class);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
            throws Exception {
        //BlackList.addBlackListByHost(ctx.channel().remoteAddress().toString());//TODO 次数限制
        cause.printStackTrace();
        ctx.close();
    }

    public NioRpcServer getNioRpcServer() {
        return nioRpcServer;
    }

    public void setNioRpcServer(NioRpcServer nioRpcServer) {
        this.nioRpcServer = nioRpcServer;
    }

    public Map<NioHandshakerType, String> getTemp() {
        return temp;
    }

    public void setTemp(Map<NioHandshakerType, String> temp) {
        this.temp = temp;
    }
}
