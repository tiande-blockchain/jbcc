package cn.tdchain.jbcc.rpc.nio.client;

import cn.tdchain.jbcc.rpc.nio.handler.NioAuthChannelHandler;
import cn.tdchain.jbcc.rpc.nio.handler.NioHandshakerType;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFactory;
import io.netty.channel.ChannelFuture;
import io.netty.util.concurrent.DefaultProgressivePromise;
import io.netty.util.concurrent.Promise;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class NioRpcClient {

    private String serverPublicKey = null;
    private String clientPublicKey = null;
    private String address = null;
    private String token = null;
    private int port;
    private long timeout = -1L;

    private Channel channel;

    public NioRpcClient(ChannelFactory channelFactory, String address, int port, long timeout, String token, String clientPublicKey){
        this.address = address;
        this.port = port;
        this.timeout = timeout;
        this.token = token;
        this.clientPublicKey = clientPublicKey;
        this.channel = connect(channelFactory, address, port, token, clientPublicKey);
    }

    private Channel connect(ChannelFactory channelFactory, String address, int port, String token, String clientPublicKey){

        try {
            Channel channel = channelFactory.newChannel();
            Promise<Map<NioHandshakerType, String>> promise = new DefaultProgressivePromise(channel.eventLoop());
            channel.pipeline().addLast(new NioAuthChannelHandler(promise, token, clientPublicKey));
            ChannelFuture future = channel.connect(new InetSocketAddress(address, port)).sync();
            channel = future.channel();
            String publicKey = null;
            try {
                publicKey = promise.sync().get().get(NioHandshakerType.SERVER_PUBLIC_KEY);
                if (publicKey == null) {
                    close();
                    return null;
                }
                this.serverPublicKey = publicKey;
                // 创建连接后remove认证handler
                channel.pipeline().remove(NioAuthChannelHandler.class);
                return channel;
            } catch (ExecutionException e) {
                e.printStackTrace();
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }


    public void close(){
        if (channel != null) {
            this.channel.close();
        }
    }

    public void send(String msg) {
        if (msg == null) {
            return;
        }
        this.channel.writeAndFlush(msg);
    }

    public boolean isActive() {
        if (channel == null) {
            return false;
        }
        return channel.isActive();
    }


    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public String getServerPublicKey() {
        return serverPublicKey;
    }
}
