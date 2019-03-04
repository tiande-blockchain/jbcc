package cn.tdchain.jbcc.rpc.nio.handler;

import cn.tdchain.cipher.DataCipher;
import cn.tdchain.jbcc.net.nio.NioNet;
import cn.tdchain.jbcc.net.nio.NioResphone;
import cn.tdchain.jbcc.rpc.RPCResult;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.util.Map;

/**
 * Synchronize NioAbstractClient Handler.
 *
 * @author murong.H 2018-08-17
 * @version 1.0
 */
public class NioResponseClientHandler extends ChannelInboundHandlerAdapter {
    private int errorNum = 0;
    private NioResphone nioResphone;
    private NioNet.NioTask task;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        String message = (String) msg;

        RPCResult r = JSONObject.parseObject(message, RPCResult.class);
        if (r != null && r.getType() == RPCResult.ResultType.batch_resphone) {
            //解密获取明文
            String cipher_data = r.getEntity();//密文DataCipher
            DataCipher data = JSONObject.parseObject(cipher_data, DataCipher.class);
            String data_str = data.getData(nioResphone.getKey().getPrivateKey(), nioResphone.getCipher());
            if (data_str == null) {
                return;
            }
            Map<String, RPCResult> connectionMap = JSON.parseObject(data_str, new TypeReference<Map<String, RPCResult>>() {
            });
            if (connectionMap != null && connectionMap.size() > 0) {
                nioResphone.getPool().add(connectionMap);//把结果添加到池中
            }
        }

        errorNum = 0;
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        if (task != null && !task.getRpcPool().isAlived()) {
            task.setStatus(false);
        }
        ctx.close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext context, Throwable cause) {
        this.errorNum++;
        System.out.println("打印:get an error times:" + errorNum);
        cause.printStackTrace();
        if (this.errorNum >= 5) {
            errorNum = 0;
            if (task != null) {
                task.setStatus(false);
            }
        }
    }

    public NioResphone getNioResphone() {
        return nioResphone;
    }

    public void setNioResphone(NioResphone nioResphone) {
        this.nioResphone = nioResphone;
    }

    public int getErrorNum() {
        return errorNum;
    }

    public void setErrorNum(int errorNum) {
        this.errorNum = errorNum;
    }

    public NioNet.NioTask getTask() {
        return task;
    }

    public void setTask(NioNet.NioTask task) {
        this.task = task;
    }
}
