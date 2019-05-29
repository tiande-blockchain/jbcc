/*
 * Copyright (c) 2017 Beijing Tiande Technology Co., Ltd.
 * All Rights Reserved.
 */
package cn.tdchain.jbcc.rpc.io.client;

import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;


/**
 * rpc client pool. 线程安全的连接对象池，支持多线程高并发使用同一个 client pool.
 *
 * @version 2.0
 * @author Xiaoming 2017-12-14
 */
public class RpcClientPool {
	private String token = "tiande@123456AbC";
	private String localPublicKey = null;
    private int maxPoolSize = 16;
    private String addres;
    private int port;
    private int timeOut = 1000;
    private String connectionId;

    private ConcurrentLinkedQueue<RpcClient> clientPool = new ConcurrentLinkedQueue<RpcClient>();

    /**
     * Constructor.
     * @param addres
     * @param port
     * @param token
     * @param connectionId
     * @param localPublicKey
     * @throws IOException
     * @throws Exception
     */
    public RpcClientPool(String addres, int port, String token,String connectionId, String localPublicKey) throws IOException,Exception {
        this(addres, port, 16, 1000, token, connectionId,localPublicKey);
    }

    /**
     * Constructor.
     * @param addres
     * @param port
     * @param maxPoolSize
     * @param token
     * @param connectionId
     * @param localPublicKey
     * @throws IOException
     * @throws Exception
     */
    public RpcClientPool(String addres, int port, int maxPoolSize, String token,String connectionId, String localPublicKey) throws IOException,Exception {
        this(addres, port, maxPoolSize, 2000, token, connectionId,localPublicKey);
    }

    /**
     * Constructor.
     * @param addres
     * @param port
     * @param maxPoolSize
     * @param timeOut
     * @param token
     * @param connectionId
     * @param localPublicKey
     * @throws IOException
     * @throws Exception
     */
    public RpcClientPool(String addres, int port, int maxPoolSize, int timeOut, String token,String connectionId,String localPublicKey) throws IOException,Exception {
        this.addres = addres;
        this.port = port;
        this.maxPoolSize = maxPoolSize;
        this.timeOut = timeOut;
        this.token = token;
        this.localPublicKey = localPublicKey;
        this.connectionId= connectionId;

        /* Init client pool */
        for (int i = 0; i < this.maxPoolSize; i++) {
        	newClient();
        }
    }

    private void newClient() throws IOException,Exception {
        if (clientPool.size() < this.maxPoolSize) {
            RpcClient client = new RpcClient(this.addres, this.port, this.timeOut, this.token, connectionId,this.localPublicKey);
            clientPool.add(client);
        }
    }

    /**
     * Description: 拿走一个链接对象.
     * @return RpcClient
     * @throws IOException
     * @throws Exception
     */
    public synchronized RpcClient getClient() throws IOException,Exception{
            if (this.clientPool.size() < 1) {
                newClient();
            }
            return this.clientPool.poll();
    }

    /**
     * Description: 归还链接对象.
     * @param client
     */
    public synchronized void returnClient(RpcClient client) {
        if (client == null) {
            return;
        }
        if (clientPool.size() < this.maxPoolSize) {
            this.clientPool.add(client);
        } else {
            // 销毁
            client.close(null);
        }
    }

    /**
     * Description: 销毁连接池.
     */
    public void drop() {
        while (clientPool.size() > 0) {
            RpcClient client = clientPool.poll();
            client.close(null);
        }
    }

    public int getMaxPoolSize() {
        return maxPoolSize;
    }

    public String getAddres() {
        return addres;
    }

    public int getPort() {
        return port;
    }

    public int getTimeOut() {
        return timeOut;
    }
}
