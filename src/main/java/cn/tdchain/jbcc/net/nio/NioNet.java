package cn.tdchain.jbcc.net.nio;

import cn.tdchain.cipher.Cipher;
import cn.tdchain.cipher.Key;
import cn.tdchain.jbcc.JbccTimeOutException;
import cn.tdchain.jbcc.PBFT;
import cn.tdchain.jbcc.ParameterException;
import cn.tdchain.jbcc.net.Net;
import cn.tdchain.jbcc.net.info.Node;
import cn.tdchain.jbcc.rpc.RPCMessage;
import cn.tdchain.jbcc.rpc.RPCResult;
import cn.tdchain.jbcc.rpc.nio.client.*;
import cn.tdchain.jbcc.rpc.nio.handler.NioPoolableRpcClientFactory;
import cn.tdchain.jbcc.rpc.nio.handler.NioRpcChannelFactory;
import cn.tdchain.jbcc.rpc.nio.handler.NioRpcClientPoolConfig;
import com.alibaba.fastjson.JSONObject;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.CharsetUtil;

import java.io.IOException;
import java.net.SocketException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * NIO版本的net实现
 *
 * @author xiaoming
 * @Description:
 * @date:上午11:44:17
 */
public class NioNet implements Net {

    private final static Bootstrap bootstrap;

    static {
        bootstrap = new Bootstrap();
        EventLoopGroup workGroup = new NioEventLoopGroup(32);
        bootstrap.group(workGroup).channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch)
                            throws Exception {
                        ch.pipeline()
                                .addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, 0, 4))
                                .addLast(new LengthFieldPrepender(4))
                                .addLast(new StringDecoder(CharsetUtil.UTF_8))
                                .addLast(new StringEncoder(CharsetUtil.UTF_8));
                    }
                });
    }

    private HashSet<String> iptables;

    /**
     * @Description: 保存着所有节点的node对象，task任务会根据node的状态去创建对应的通信任务。初次启动需要net开启一个异步线程去获取iptables里面的节点对应的node。
     */
    private Hashtable<String, Node> nodes = new Hashtable<>();

    private int serverPort;

    private Cipher cipher;

    private String token;

    private Key key;

    private String connectionId;

    private int minResult = 1;

    private int minOnlineNodes = 1;//最小在线节点数

    private int true_count = 0;//如果又达到2/3的node了就可以往下继续。

    /**
     * @Description: 在线节点的任务列表
     */
    private HashMap<String, NioTask> taskList = new HashMap<>();

    public NioNet(String[] iptables, int serverPort, Cipher cipher, String token, Key key, String connectionId) {
        this.iptables = new HashSet<>(Arrays.asList(iptables));
        this.serverPort = serverPort;
        this.cipher = cipher;
        this.token = token;
        this.key = key;
        this.connectionId = connectionId;

        /**
         * @Description: 根据初始化的iptable长度计算最小返回结果集大小。
         */
        this.minResult = PBFT.getMinByCount(iptables.length);
    }


    @Override
    public void start() {
        asynGetNodesByIpTable();
        // 动态运行每个节点的Task
        new Thread(() -> {
            while (true) {
                try {
                    System.out.println("Net node size=" + nodes.size());
                    System.out.println("Net task size=" + taskList.size());
                    Iterator<String> id_i = nodes.keySet().iterator();
                    while (id_i.hasNext()) {
                        String id = id_i.next();
                        Node node = nodes.get(id);
                        System.out.println("node id=" + node.getId() + "  serverip=" + node.serverIP() + "  status=" + node.getStatus());

                        NioTask t = null;
                        t = taskList.get(id);
                        if (t == null) {
                            try {
                                t = new NioTask(node.serverIP(), serverPort, cipher, token, key, connectionId, 3);
                                t.start();// 开启任务
                                taskList.put(id, t);
                            } catch (Exception e) {
                                e.printStackTrace();
                                continue;
                            }

                        } else {
                            System.out.println("task id=" + id + "  status=" + t.status);
                        }
                        // 可能网络原因、或者节点下线造成通信失败的，该任务需要销毁。
                        if (t != null && !t.status) {
                            t.stop();// 结束任务
                            taskList.remove(id);
                        }
                    }


                    //休息
                    Thread.sleep(2000);
                } catch (Exception e) {
                }


            }

        }).start();
    }

    @Override
    public void request(RPCMessage msg) {
        taskList.forEach((k, v) -> {
            if (v != null && v.status) {
                msg.setTarget(k);
                v.addRequest(msg.clone());
            } else {
//				System.out.println("丢弃消息");
            }

        });

    }

    @Override
    public List<RPCResult> resphone(String messageId, long timeOut) {
        List<RPCResult> r_list = new ArrayList<RPCResult>(this.minResult + 3);
        long start = System.currentTimeMillis();

        List<NioTask> task_list = new ArrayList<>(taskList.size() + 3);
        taskList.forEach((k, v) -> {
            if (v != null) {
                task_list.add(v);
            }
        });

        while (true) {
            task_list.forEach((v) -> {
                if (v != null) {
                    RPCResult r = v.poll(messageId);// 弹出消息，没有则返回空

                    if (r != null) {
                        r_list.add(r);// 接收一个非null的结果
                    }
                }
            });


            if ((r_list.size() >= taskList.size())) {
                break;//结果集达到要求或者超时时间到达就退出while循环
            }

            if ((System.currentTimeMillis() - start) > timeOut) {
                if (r_list.size() >= 1) {
                    System.out.println("r_list.size()=" + r_list.size());
                    break;
                }
                //抛出超时异常
                throw new JbccTimeOutException("jbcc request timeout:" + timeOut);
            }

            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
            }
        }

        return r_list;
    }

    @Override
    public int getTaskSize() {
        synchronized (taskList) {
            return taskList.size();
        }
    }

    @Override
    public int getMinNodeSize() {
        minOnlineNodes = 0;
        this.nodes.forEach((k, v) -> {
            if (v.getStatus() == Node.NodeStatus.METRONOMER) {
                minOnlineNodes++;
            }
        });
        if (minOnlineNodes == 0) {
            minOnlineNodes = 1;
        }

        return PBFT.getMinByCount(minOnlineNodes);
    }

    @Override
    public void addNodeToNodes(Node node) {
        //只是新加新的node对象
        if (this.nodes.get(node.getId()) == null) {
            this.nodes.put(node.getId(), node);
        } else {
            Node n = this.nodes.get(node.getId());
            n.setStatus(node.getStatus());//只更新状态
        }
    }


    public class NioTask {
        private boolean status = true;// true=在线     false=死忙的

        private NioRpcClientPool rpcPool;

        private NioRequest request;

        private NioResphone resphone;


        public NioTask(String serverHost, int serverPort, Cipher cipher, String token, Key key, String connectionId, int workerNum) {
            // 初始化sever 公钥
            String serverPublicKey = null;
            try {
                this.rpcPool = createPool(serverHost, serverPort, token, key.getPublicKey());
                NioRpcClient client = this.rpcPool.borrowObject();
                serverPublicKey = client.getServerPublicKey();
                this.rpcPool.returnObject(client);
            } catch (SocketException e) {
                throw new ParameterException("get NioRpcClientPool SocketException, target ip=" + serverHost + "  :" + e.getMessage());
            } catch (IOException e) {
                throw new ParameterException("get NioRpcClientPool IOException, target ip=" + serverHost + "  :" + e.getMessage());
            } catch (Exception e) {
                throw new ParameterException("get NioRpcClientPool Exception, target ip=" + serverHost + "  :" + e.getMessage());
            }

            request = new NioRequest(this, serverHost, serverPort, cipher, token, key, connectionId, 3, serverPublicKey);
            resphone = new NioResphone(this, serverHost, connectionId, key, cipher, 3, serverPublicKey);
        }

        private NioRpcClientPool createPool(String serverHost, int serverPort, String token, String publicKey) {
            NioRpcClientPoolConfig config = new NioRpcClientPoolConfig(new NioRpcChannelFactory(bootstrap), serverHost, serverPort, 5000, token, publicKey);
            config.setMaxTotal(16);
            config.setMaxIdle(8);
            NioPoolableRpcClientFactory factory = new NioPoolableRpcClientFactory(config, this);
            NioRpcClientPool pool = new NioRpcClientPool(factory, config);
            return pool;
        }

        /**
         * @param msg
         * @throws
         * @Description: 把交易信息添加给 request
         */
        public void addRequest(RPCMessage msg) {
            if (this.status) {
                this.request.addRequest(msg);
            }
        }

        /**
         * @param messageId
         * @return
         * @throws
         * @Description: 获取交易
         */
        public RPCResult poll(String messageId) {
            return this.resphone.poll(messageId);
        }


        public boolean isStatus() {
            return status;
        }

        public void start() {
            this.status = true;
            this.request.start();
            this.resphone.start();
        }

        public void stop() {
            this.status = false;
            this.request.stop();
            this.resphone.stop();

            //销毁连接池
            this.rpcPool.close();
            if (this.rpcPool.isClosed()) {
                this.rpcPool.clear();
            }
        }


        public NioRpcClient getClient() {
            try {
                return this.rpcPool.borrowObject();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        public <T> T getClient(Class<T> clazz) {
            if (clazz == NioRpcResponseClient.class) {
                NioRpcResponseClient nioRpcResponseClient = new NioRpcResponseClient(this.getClient());
                return (T) nioRpcResponseClient;
            }
            if (clazz == NioRpcSynResponseClient.class) {
                NioRpcSynResponseClient nioResClient = new NioRpcSynResponseClient(this.getClient());
                return (T) nioResClient;
            }
            return (T) getClient();
        }


        public void returnClient(NioRpcClient client) {
            this.rpcPool.returnObject(client);
        }

        public void setStatus(boolean status) {
            this.status = status;
        }

        public NioRpcClientPool getRpcPool() {
            return rpcPool;
        }

        public void setRpcPool(NioRpcClientPool rpcPool) {
            this.rpcPool = rpcPool;
        }
    }


    /**
     * @throws
     * @Description: 异步获取iptable中的对应的node对象
     */
    private void asynGetNodesByIpTable() {
        new Thread(new Runnable() {

            @Override
            public void run() {
                //初始化
                HashMap<String, Boolean> flag = new HashMap<String, Boolean>();
                Iterator<String> ip_i = iptables.iterator();
                HashMap<String, NioRpcSynResponseClient> clients = new HashMap<>();
                while (ip_i.hasNext()) {
                    String ip = ip_i.next();
                    if (ip != null && ip.length() > 0) {
                        flag.put(ip, false);
                    }
                }

                while (true) {
                    // 1秒后开始
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e1) {
                    }

                    Iterator<String> server_i = flag.keySet().iterator();
                    while (server_i.hasNext()) {
                        String server = null;
                        try {
                            server = server_i.next();
                            boolean flag_v = flag.get(server);
                            if (!flag_v) {
                                // server还没有请求到对应的node对象-->继续
                                NioRpcSynResponseClient client = clients.get(server);
                                if (client == null) {
                                    client = new NioRpcSynResponseClient(new NioRpcClient(new NioRpcChannelFactory(bootstrap), server, serverPort, 3000, token, key.getPublicKey()));
                                    if (client.isActive()) {
                                        clients.put(server, client);
                                    } else {
                                        continue;
                                    }
                                }


                                // 封装请求休息体
                                RPCMessage requesy_node = new RPCMessage();
                                requesy_node.setTarget(server);
                                requesy_node.setTargetType(RPCMessage.TargetType.REQUEST_NODE);
                                requesy_node.setSender(connectionId);//

                                String r_str = client.sendAndReturn(requesy_node.toJsonString(), 5000);
                                if (r_str != null) {
                                    RPCResult r = JSONObject.parseObject(r_str, RPCResult.class);
                                    if (r != null && r.getType() == RPCResult.ResultType.resphone_node) {
                                        String data = r.getEntity();
                                        if (data != null) {
                                            Node node = JSONObject.parseObject(data, Node.class);
                                            if (node != null) {
                                                // 收到一个node
                                                System.out.println("初次收到node=" + node.toJSONString());
                                                nodes.put(node.getId(), node);

                                                //关闭连接资源
                                                client.close();
                                                clients.remove(server);

                                                // 标记已经收到一个node
                                                flag.put(server, true);

                                            }
                                        }

                                    }

                                }

                            }

                        } catch (Exception e) {
                            System.out.println("request error server ip=" + server);
                            e.printStackTrace();
                        }
                    }

                    //只要获取到了iptables数的2/3 node状态就可以继续
                    flag.forEach((k, v) -> {
                        if (v) {
                            true_count++;
                        }
                    });
                    if (true_count >= minResult) {
                        // 已经获取全部node
                        break;
                    }

                }
            }
        }).start();
    }

    @Override
    public List<Node> getNodes() {
        return this.nodes.entrySet().stream().filter(en -> en.getValue() != null).map(en -> en.getValue()).collect(Collectors.toList());
    }
}
