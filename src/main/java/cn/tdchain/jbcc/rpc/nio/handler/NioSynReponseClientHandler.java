package cn.tdchain.jbcc.rpc.nio.handler;

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
        this.promise.trySuccess(message);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        ctx.close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext context, Throwable cause) {
    }

    public Promise<String> getPromise() {
        return promise;
    }

    public void setPromise(Promise<String> promise) {
        this.promise = promise;
    }
}
