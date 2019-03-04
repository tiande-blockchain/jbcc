package cn.tdchain.jbcc.rpc.nio.handler;

import cn.tdchain.jbcc.net.nio.NioNet;
import cn.tdchain.jbcc.rpc.nio.client.NioRpcClient;
import io.netty.channel.ChannelFactory;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;

public class NioPoolableRpcClientFactory extends BasePooledObjectFactory<NioRpcClient> {

    private ChannelFactory channelFactory;
    private String clientPublicKey = null;
    private String address = null;
    private String token = null;
    private int port;
    private long timeout = -1L;

    private NioNet.NioTask task;

    public NioPoolableRpcClientFactory(NioRpcClientPoolConfig config, NioNet.NioTask task) {
        super();
        this.channelFactory = config.getChannelFactory();
        this.address = config.getAddress();
        this.port = config.getPort();
        this.timeout = config.getTimeout();
        this.token = config.getToken();
        this.clientPublicKey = config.getClientPublicKey();
        this.task = task;
    }

    @Override
    public NioRpcClient create() {
        NioRpcClient client = new NioRpcClient(channelFactory, address, port, timeout, token, clientPublicKey);
        //client.getChannel().pipeline().get(ReceiveSysMessageChannelHandler.class).setTask(task);
        return client;
    }

    @Override
    public PooledObject<NioRpcClient> wrap(NioRpcClient client) {
        return new DefaultPooledObject<>(client);
    }

    @Override
    public void passivateObject(PooledObject<NioRpcClient> object) {
        if (object != null) {
            NioRpcClient client = object.getObject();
            if (!client.isActive()) {
                try {
                    destroyObject(wrap(client));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void destroyObject(PooledObject<NioRpcClient> object) {
        if (object != null) {
            NioRpcClient client = (NioRpcClient) object;
            client.close();
        }
    }

}
