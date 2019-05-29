/*
 * Copyright (c) 2017 Beijing Tiande Technology Co., Ltd.
 * All Rights Reserved.
 */
package cn.tdchain.jbcc.net.io;

import cn.tdchain.cipher.Cipher;
import cn.tdchain.cipher.Key;
import cn.tdchain.jbcc.JbccTimeOutException;
import cn.tdchain.jbcc.PBFT;
import cn.tdchain.jbcc.ParameterException;
import cn.tdchain.jbcc.SoutUtil;
import cn.tdchain.jbcc.net.Net;
import cn.tdchain.jbcc.net.info.Node;
import cn.tdchain.jbcc.rpc.RPCBatchResult;
import cn.tdchain.jbcc.rpc.RPCMessage;
import cn.tdchain.jbcc.rpc.RPCResult;
import cn.tdchain.jbcc.rpc.io.client.RpcClient;
import cn.tdchain.jbcc.rpc.io.client.RpcClientPool;
import com.alibaba.fastjson.JSONObject;

import java.io.IOException;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

/**
 * Description: 网络层
 * @author xiaoming
 * 2019年4月18日
 */
public class IONet implements Net {

    private HashSet<String> iptables = null;

    /**
     * Description: 保存着所有节点的node对象，task任务会根据node的状态去创建对应的通信任务。初次启动需要net开启一个异步线程去获取iptables里面的节点对应的node。
     */
    private Hashtable<String, Node> nodes = new Hashtable<String, Node>();

    private int serverPort = 18088;

    private Cipher cipher;

    private String token;

    private Key key;

    private String connectionId;

    private int minResult = 1;

    private int minOnlineNodes = 1;//最小在线节点数

    private int true_count = 0;//如果又达到2/3的node了就可以往下继续。

    private boolean status = true;// true=在线     false=死忙的

    /**
     * Description: 在线节点的任务列表
     */
    private HashMap<String, Task> taskList = new HashMap<String, Task>();

    public IONet(String[] iptables, int serverPort, Cipher cipher, String token, Key key, String connectionId) {
        this.iptables = new HashSet<String>(Arrays.asList(iptables));
        this.serverPort = serverPort;
        this.cipher = cipher;
        this.token = token;
        this.key = key;
        this.connectionId = connectionId;

        /**
         * Description: 根据初始化的iptable长度计算最小返回结果集大小。
         */
        this.minResult = PBFT.getMinByCount(iptables.length);
    }

    /**
     * Description: 向net网络中添加一个新的目标主机, 防止重复的ip，一台主机可能存在多个ip段。
     */
    public void addNodeToNodes(Node node) {
        //只是新加新的node对象
        if (this.nodes.get(node.getId()) == null) {
            this.nodes.put(node.getId(), node);
        } else {
            Node n = this.nodes.get(node.getId());
            n.setStatus(node.getStatus());//只更新状态
        }
    }

    /**
     * Description: 异步获取iptable中的对应的node对象
     */
    private void asynGetNodesByIpTable() {
        new Thread(new Runnable() {

            @Override
            public void run() {
                //初始化
                HashMap<String, Boolean> flag = new HashMap<String, Boolean>();
                Iterator<String> ip_i = iptables.iterator();
                HashMap<String, RpcClient> clients = new HashMap<String, RpcClient>();
                while (ip_i.hasNext()) {
                    String ip = ip_i.next();
                    if (ip != null && ip.length() > 0) {
                        flag.put(ip, false);
                    }
                }

                while (true) {
                    if (status) {
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
                                    RpcClient client = clients.get(server);
                                    if (client == null) {
                                        client = new RpcClient(server, serverPort, 3000, token, connectionId, key.getLocalCertBase64String());
                                        clients.put(server, client);
                                    }


                                    // 封装请求休息体
                                    RPCMessage requesy_node = new RPCMessage();
                                    requesy_node.setTarget(server);
                                    requesy_node.setTargetType(RPCMessage.TargetType.REQUEST_NODE);
                                    requesy_node.setSender(connectionId);//

                                    String r_str = client.sendAndReturn(requesy_node.toJsonString());
                                    if (r_str != null) {
                                        RPCResult<Node> r = JSONObject.parseObject(r_str, RPCResult.class);
                                        if (r != null && r.getType() == RPCResult.ResultType.resphone_node) {
                                            if (r.getEntity() != null) {
                                                Node node = r.getEntity();
                                                if (node != null) {
                                                    // 收到一个node
                                                    if (SoutUtil.isOpenSout())
                                                        System.out.println("初次收到node=" + node.toJSONString());
                                                    nodes.put(node.getId(), node);

                                                    //关闭连接资源
                                                    client.close(null);
                                                    clients.remove(server);

                                                    // 标记已经收到一个node
                                                    flag.put(server, true);

                                                }
                                            }

                                        }

                                    }

                                }

                            } catch (Exception e) {
                                if (SoutUtil.isOpenSout())
                                    System.out.println("request error server ip=" + server);
                                e.printStackTrace();
                            }
                        }

                        //只要获取到了iptables数的2/3 node状态就可以继续
                        true_count = 0;
                        flag.forEach((k, v) -> {
                            if (v) {
                                true_count++;
                            }
                        });
                        if (true_count >= minResult) {
                            // 已经获取全部node
                            break;
                        }
                    } else {
                        break;//结束net
                    }


                }
            }
        }).start();
    }

    /**
     * Description: 开启网络功能
     */
    public void start() {
        // 异步获取iptable中的对应的node对象
        asynGetNodesByIpTable();

        // 动态运行每个节点的Task
        new Thread(new Runnable() {

            @Override
            public void run() {

                while (true) {
                    if (status) {
                        try {
                            if (SoutUtil.isOpenSout()) {
                                System.out.println("Net node size=" + nodes.size());
                                System.out.println("Net task size=" + taskList.size());
                            }
                            Iterator<String> id_i = nodes.keySet().iterator();
                            while (id_i.hasNext()) {
                                String id = id_i.next();
                                Node node = nodes.get(id);

                                if (SoutUtil.isOpenSout())
                                    System.out.println("node id=" + node.getId() + "  serverip=" + node.serverIP() + "  status=" + node.getStatus());

                                Task t = null;

                                t = taskList.get(id);
                                if (!Node.NodeStatus.DIE.equals(node.getStatus())) {
//									synchronized (taskList) {
                                    if (t == null) {
                                        try {
                                            t = new Task(node.serverIP(), serverPort, cipher, token, key, connectionId,
                                                    1);
                                            t.start();// 开启任务
                                            taskList.put(id, t);
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                            continue;
                                        }

                                    } else {
                                        if (SoutUtil.isOpenSout())
                                            System.out.println("task id=" + id + "  status=" + t.status);
                                    }
//									}

                                } else {
                                    // 节点死忙
                                    if (t != null && t.status) { //如果节点die，task还在连接，则任务需要销毁
                                        t.stop();
                                        taskList.remove(id);
                                    }

                                    if (SoutUtil.isOpenSout())
                                        System.out.println("dead node id=" + node.getId());
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
                    } else {
                        taskList.forEach((k, v) -> {
                            v.stop();//结束task
                        });
                        break;//结束net
                    }


                }

            }
        }).start();

    }


    /**
     * Description: 将消息添加到各个节点的task上
     */
    public void request(RPCMessage msg) {
        taskList.forEach((k, v) -> {
            if (v != null && v.status) {
                msg.setTarget(k);
                v.addRequest(msg.clone());//不同task 一定存放着不同的msg对象，否则可能会出现线程安全问题。
            } else {
//				System.out.println("丢弃消息");
            }

        });

    }

    /**
     * Description: 根据messageId等待获取结果，设置超时时间。等待接受iptable *2/3 结果list。
     */
   /* public List<RPCResult> resphone(String messageId, long timeOut) {
        List<RPCResult> r_list = new ArrayList<RPCResult>(this.minResult + 3);
        long start = System.currentTimeMillis();

        List<Task> task_list = new ArrayList<Task>(taskList.size() + 3);
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


            if ((r_list.size() >= minResult)) {
                break;//结果集达到要求或者超时时间到达就退出while循环
            }

            if ((System.currentTimeMillis() - start) > timeOut) {
                if (r_list.size() >= 1) {
                    if (SoutUtil.isOpenSout())
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
    }*/

    @Override
    public RPCBatchResult resphone(String messageId, long timeOut) {
        return null;
    }


    /**
     * Description: 一个节点对应一个task
     * @author xiaoming
     * 2019年4月18日
     */
    public class Task {
        private boolean status = true;// true=在线     false=死忙的

        private RpcClientPool rpcPool;

        private Request request;

        private Resphone resphone;


        public Task(String serverHost, int serverPort, Cipher cipher, String token, Key key, String connectionId, int workerNum) {
            // 初始化sever 公钥
            String serverPublicKey = null;

            try {
                this.rpcPool = new RpcClientPool(serverHost, serverPort, workerNum * 2, token, connectionId, key.getLocalCertBase64String());

                RpcClient client = this.rpcPool.getClient();
                serverPublicKey = client.getServerPublicKey();
                this.rpcPool.returnClient(client);
            } catch (SocketException e) {
                throw new ParameterException("get AIORpcClientPool SocketException, target ip=" + serverHost + "  :" + e.getMessage());
            } catch (IOException e) {
                throw new ParameterException("get AIORpcClientPool IOException, target ip=" + serverHost + "  :" + e.getMessage());
            } catch (Exception e) {
                throw new ParameterException("get AIORpcClientPool Exception, target ip=" + serverHost + "  :" + e.getMessage());
            }


            request = new Request(this, serverHost, serverPort, cipher, token, key, connectionId, workerNum, serverPublicKey);
            resphone = new Resphone(this, serverHost, connectionId, key, cipher, workerNum, serverPublicKey);
        }


        /**
         * Description: 把交易信息添加给 request
         * @param msg
         */
        public void addRequest(RPCMessage msg) {
            if (this.status) {
                this.request.addRequest(msg);
            }
        }

        /**
         * Description: 获取交易
         * @param messageId
         * @return
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
            this.rpcPool.drop();
        }


        public RpcClient getClient() throws IOException, Exception {
            return this.rpcPool.getClient();
        }


        public void returnClient(RpcClient client) {
            this.rpcPool.returnClient(client);
        }
    }

    /**
     * Description: 获取活跃的task数
     */
    public int getTaskSize() {
        synchronized (taskList) {
            return taskList.size();
        }
    }

    /**
     * Description: 获取所有在线node的2/3
     */
    public int getMinNodeSize() {
        minOnlineNodes = 0;
        this.nodes.forEach((k, v) -> {
            if (v.getStatus() == Node.NodeStatus.METRONOMER) {
                minOnlineNodes++;
            }
        });

        // 判断根据iptable上计算得出的 minOnlineNodes  比较哪个大用哪个。
        int min = PBFT.getMinByCount(this.iptables.size());
        if (min > minOnlineNodes) {
            minOnlineNodes = min;
        }


        if (minOnlineNodes == 0) {
            minOnlineNodes = 1;
        }

        return PBFT.getMinByCount(minOnlineNodes);
    }

    @Override
    public List<Node> getNodes() {
        List<Node> nodes = new ArrayList<Node>();
        this.nodes.forEach((k, v) -> {
            if (v != null) {
                nodes.add(v);
            }
        });

        return nodes;
    }

    @Override
    public void stop() {
        this.status = false;
    }

}
