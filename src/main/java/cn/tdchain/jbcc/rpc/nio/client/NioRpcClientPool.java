package cn.tdchain.jbcc.rpc.nio.client;

import cn.tdchain.jbcc.TimerUtil;
import cn.tdchain.jbcc.rpc.nio.handler.NioPoolableRpcClientFactory;
import cn.tdchain.jbcc.rpc.nio.handler.NioRpcClientPoolConfig;
import org.apache.commons.pool2.impl.GenericObjectPool;

public class NioRpcClientPool extends GenericObjectPool<NioRpcClient> {
    private NioRpcClient nioRpcClient;

    public NioRpcClientPool(NioPoolableRpcClientFactory factory, NioRpcClientPoolConfig config) {
        super(factory, config);
    }

    public boolean isAlived() {
        if (nioRpcClient == null || !nioRpcClient.isActive()) {
            NioRpcClient np = TimerUtil.exec(0, 3, () -> {
                NioRpcClient nioRpcClient = this.borrowObject();
                if (nioRpcClient == null) {
                    return null;
                }
                return nioRpcClient;
            });
            if (np == null) {
                return false;
            }
            this.nioRpcClient = np;
            return np.isActive();
        }
        return nioRpcClient.isActive();
    }
}
