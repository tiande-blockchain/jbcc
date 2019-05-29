/*
 * Copyright (c) 2017 Beijing Tiande Technology Co., Ltd.
 * All Rights Reserved.
 */
package cn.tdchain.jbcc.net.nio;

import cn.tdchain.cipher.Cipher;
import cn.tdchain.cipher.DataCipher;
import cn.tdchain.cipher.Key;
import cn.tdchain.jbcc.SoutUtil;
import cn.tdchain.jbcc.net.nio.NioNet.NioTask;
import cn.tdchain.jbcc.rpc.RPCMessage;
import cn.tdchain.jbcc.rpc.nio.client.NioRpcClient;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Description: 异步提交请求消息
 * @author xiaoming
 * 2019年4月18日
 */
public class NioRequest {

    private NioTask task;

    private int workerNum = 1;// 工人数，默认1名.

    private NioRequestPool pool;

    private String ip;

    private Cipher cipher;

    private String token;

    private Key key;

    private boolean status = true;// true=在线     false=死忙的

    private String connectionId;

    private String serverPublicKey = null;// 缓存server 的公钥


    public NioRequest(NioTask task, String serverHost, int serverPort, Cipher cipher, String token, Key key, String connectionId, int workerNum, String serverPublicKey) {
        this.task = task;
        this.connectionId = connectionId;
        this.key = key;
        this.token = token;
        this.cipher = cipher;
        this.ip = serverHost;
        if (workerNum > this.workerNum) {
            this.workerNum = workerNum;
        }
        this.serverPublicKey = serverPublicKey;

        this.pool = new NioRequestPool();
    }

    int error_num = 0;

    public void start() {
        this.pool.start();

        long wait_time = 50;
        for (int i = 0; i < workerNum; i++) {
            new Thread(new Runnable() {
                private int close(int error_num, NioRpcClient client, Exception e) {
                    error_num++;
                    if (client != null) {
                        client.close();
                    }
                    return error_num;
                }

                @Override
                public void run() {
                    NioRpcClient client = task.getClient();
                    while (status) {
                        List<RPCMessage> msgList = pool.getMsgList(1000);
                        if (msgList.size() > 0) {
                            //封装批量消息体
                            RPCMessage batch_msg = new RPCMessage();
                            batch_msg.setTarget(ip);
                            batch_msg.setTargetType(RPCMessage.TargetType.BATCH_REQUEST);
                            batch_msg.setSender(connectionId);//
                            batch_msg.setMsg(JSONObject.toJSONString(msgList));

                            /** start 数字信封发送 */
                            DataCipher data = new DataCipher(UUID.randomUUID().toString(), batch_msg.getMsg(), key.getPrivateKey(),
                                    serverPublicKey, cipher);
                            batch_msg.setMsg(JSON.toJSONString(data));// 更新密文发送
                            /** end 数字信封发送 */

                            //发送如果出现网络异常超过三次则判断为该节点下线。结束本任务
                            try {
                                //获取连接对象
                                if (client == null) {
                                    client = task.getClient();
                                }
                                // 发送
                                client.send(batch_msg.toJsonString());
                                //重置error_num = 0
                                error_num = 0;
                            } catch (Exception e) {
                                error_num = close(error_num, client, e);
                            }
                            if (error_num > 5) {
                                if (SoutUtil.isOpenSout())
                                    System.out.println("request task 被销毁");
                                task.stop();//可能出现网络异常，需要结束整个task任务。
                            }
                        }
                        //间接休息
                        try {
                            Thread.sleep(wait_time);
                        } catch (InterruptedException e) {
                        }
                    }
                }
            }).start();
        }

    }

    public void addRequest(RPCMessage msg) {
        if (this.status) {
            pool.add(msg);
        }
    }


    public void stop() {
        this.status = false;
        this.pool.stop();
    }


    /**
     * Description: 请求任务的消息池
     * @author xiaoming
     * 2019年4月18日
     */
    public class NioRequestPool {
        private boolean status = true;

        private LinkedBlockingQueue<RPCMessage> queue = new LinkedBlockingQueue<RPCMessage>();

        public void add(RPCMessage msg) {
            if (this.status) {
                queue.add(msg);
            }
        }


        /**
         * Description: 批量获取消息列表
         * @param maxSize
         * @return
         */
        public List<RPCMessage> getMsgList(int maxSize) {
            List<RPCMessage> msgList = new ArrayList<RPCMessage>();
            for (int i = 0; i < maxSize; i++) {
                RPCMessage msg = queue.poll();
                if (msg != null) {
                    msgList.add(msg);
                }
            }
            return msgList;
        }

        public void start() {
            this.status = true;
        }

        public void stop() {
            this.status = false;
        }

    }


}
