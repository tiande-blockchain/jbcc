package cn.tdchain.jbcc.net.nio;

import cn.tdchain.cipher.Cipher;
import cn.tdchain.cipher.Key;
import cn.tdchain.jbcc.TimerUtil;
import cn.tdchain.jbcc.net.nio.NioNet.NioTask;
import cn.tdchain.jbcc.rpc.RPCMessage;
import cn.tdchain.jbcc.rpc.RPCResult;
import cn.tdchain.jbcc.rpc.nio.handler.NioResponseClientHandler;
import cn.tdchain.jbcc.rpc.nio.client.NioRpcResponseClient;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author murong
 * @Description: 异步提交请求消息
 * @date:上午10:59:25
 */
public class NioResphone {
    private NioTask task;

    private String ip;

    private int workerNum = 3;// 工人数，默认3名.

    private NioResphonePool pool;

    private Cipher cipher;

    private Key key;

    private boolean status = true;// true=在线     false=死忙的

    private String connectionId;

    private String serverPublicKey;


    public NioResphone(NioTask task, String serverIp, String connectionId, Key key, Cipher cipher, int workerNum, String serverPublicKey) {
        this.task = task;
        this.ip = serverIp;
        this.connectionId = connectionId;
        this.key = key;
        this.cipher = cipher;
        if (workerNum > this.workerNum) {
            this.workerNum = workerNum;
        }
        this.serverPublicKey = serverPublicKey;

        this.pool = new NioResphonePool(task);
    }

    public void start() {
        this.pool.start(); //开启消息池的异步任务
        // 开始异步批量提交请求
        if (this.status) {
            //封装批量消息体
            RPCMessage batch_msg = new RPCMessage();
            batch_msg.setTarget(ip);
            batch_msg.setTargetType(RPCMessage.TargetType.BATCH_RESPHONE);
            batch_msg.setSender(connectionId);//
            try {
                NioRpcResponseClient client = TimerUtil.exec(0, 5, () -> {
                    NioRpcResponseClient nc = task.getClient(NioRpcResponseClient.class);
                    if (nc == null || !nc.isActive()) {
                        return null;
                    }
                    return nc;
                });
                if (client == null) {
                    task.stop();
                    return;
                }
                NioResponseClientHandler clientHandler = client.getNioRpcClient().getChannel().pipeline().get(NioResponseClientHandler.class);
                clientHandler.setNioResphone(NioResphone.this);
                clientHandler.setTask(task);
                //获取连接对象
                client.send(batch_msg.toJsonString());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }


    public void stop() {
        this.status = false;
        this.pool.stop();
    }

    /**
     * @param messageId
     * @return
     * @throws
     * @Description: 根据消息id弹出消息结果，如果没有则返回空。
     */
    public RPCResult poll(String messageId) {
        return this.pool.poll(messageId);
    }

    /**
     * @author xiaoming
     * @Description: 返回结果池
     * @date:上午10:59:04
     */
    public class NioResphonePool {
        private NioTask task;

        private boolean status = true;// true=在线     false=死忙的

        public NioResphonePool(NioTask task) {
            this.task = task; //注入本次task对象
        }

        private ConcurrentHashMap<String, RPCResult> resphonePool = new ConcurrentHashMap<String, RPCResult>();

        /**
         * @param messageId
         * @return
         * @throws
         * @Description: 根据消息id获取返回结果对象
         */
        public RPCResult poll(String messageId) {
            try {
//				synchronized(resphonePool) {
                return resphonePool.remove(messageId);
//				}
            } catch (Exception e) {
            }
            return null;
        }

        public synchronized void add(Map<String, RPCResult> connectionMap) {
//			synchronized(resphonePool) {
            resphonePool.putAll(connectionMap);
//			}
        }

        public void stop() {
            this.status = false;
        }

        public void start() {
            this.status = true;
            // 开始异步扫描pool中过时的result对象
            checkTimeOut(4000);
        }

        /**
         * @throws
         * @Description: 异步扫描过时的结果对象
         */
        private void checkTimeOut(long timeOut) {
            new Thread(() -> {
                while (true) {
                    if (status) {
                        long now_time = System.currentTimeMillis();
                        try {
//								List<String> messageIds = new ArrayList<String>();

                            /** 异常类型消息检查 */
                            RPCResult error_r = resphonePool.remove(RPCResult.PRC_RESULT_DESCRYPT_ERROR);
                            if ((error_r != null) && (error_r.getType() == RPCResult.ResultType.msg_error)) {
                                if (error_r.getStatus() == RPCResult.StatusType.fail) {
                                    //收到此返回消息说明服务端解密出现异常，可能连接对象不对称，需要关闭连接后重新连接。
                                    task.stop();//整个任务结束
                                    return;//随后结束线程
                                }
                            }

                            resphonePool.forEach((k, v) -> {
                                if (v != null) {
                                    // 消除过时的 RPCResult 对象
                                    if ((now_time - v.getStartTime()) > timeOut) {
                                        resphonePool.remove(v.getMessageId());
                                    }
                                }

                            });


                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        break;//结束本循环工作。
                    }

                    //4秒循环坚检查一次
                    try {
                        Thread.sleep(timeOut * 2);
                    } catch (InterruptedException e) {
                    }
                }
            }).start();
        }

    }

    public NioTask getTask() {
        return task;
    }

    public void setTask(NioTask task) {
        this.task = task;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getWorkerNum() {
        return workerNum;
    }

    public void setWorkerNum(int workerNum) {
        this.workerNum = workerNum;
    }

    public NioResphonePool getPool() {
        return pool;
    }

    public void setPool(NioResphonePool pool) {
        this.pool = pool;
    }

    public Cipher getCipher() {
        return cipher;
    }

    public void setCipher(Cipher cipher) {
        this.cipher = cipher;
    }

    public Key getKey() {
        return key;
    }

    public void setKey(Key key) {
        this.key = key;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public String getConnectionId() {
        return connectionId;
    }

    public void setConnectionId(String connectionId) {
        this.connectionId = connectionId;
    }

    public String getServerPublicKey() {
        return serverPublicKey;
    }

    public void setServerPublicKey(String serverPublicKey) {
        this.serverPublicKey = serverPublicKey;
    }
}