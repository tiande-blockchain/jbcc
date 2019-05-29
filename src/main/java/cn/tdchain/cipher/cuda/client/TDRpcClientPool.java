/*
 * Copyright (c) 2017 Beijing Tiande Technology Co., Ltd.
 * All Rights Reserved.
 */
package cn.tdchain.cipher.cuda.client;

import cn.tdchain.jbcc.SoutUtil;

import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * 
 * @author xiaoming
 * 2019年4月18日
 */
public class TDRpcClientPool {

    private int maxPoolSize = 16;
    private String addres;
    private int port;
    private int timeOut = 1000;
    private int minPoolSize = 1;

    private ConcurrentLinkedQueue<TDRpcClient> clientPool = new ConcurrentLinkedQueue<TDRpcClient>();

    /**
     * Constructor.
     *
     * @param addres String
     * @param port   int
     * @throws IOException
     */
    public TDRpcClientPool(String addres, int port) throws IOException {
        this(addres, port, 16, 1000);
    }

    /**
     * Constructor.
     *
     * @param addres      String
     * @param port        int
     * @param maxPoolSize int
     * @throws IOException
     */
    public TDRpcClientPool(String addres, int port, int maxPoolSize) throws IOException {
        this(addres, port, maxPoolSize, 1000);
    }

    /**
     * Constructor.
     *
     * @param addres      String
     * @param port        int
     * @param maxPoolSize int
     * @param timeOut     int, default 1000
     * @throws IOException
     */
    public TDRpcClientPool(String addres, int port, int maxPoolSize,
                           int timeOut) throws IOException {
        this.addres = addres;
        this.port = port;
        this.maxPoolSize = maxPoolSize;
        this.timeOut = timeOut;
        int minPoolSize = maxPoolSize / 10;
        if (minPoolSize > 1) {
            this.minPoolSize = minPoolSize;
        }

        /* Init client pool */
        for (int i = 0; i < this.maxPoolSize; i++) {
            newClient();
        }
    }

    private void newClient() throws IOException {
        if (clientPool.size() < this.maxPoolSize) {
            TDRpcClient client = new TDRpcClient(this.addres, this.port, this.timeOut);
            clientPool.add(client);
        }
    }

    /**
     * @return TDRpcClient
     */
    public TDRpcClient getClient() {
        TDRpcClient c = null;
        try {
            long start = System.currentTimeMillis();
            while (true) {
                synchronized (this) {
                    c = this.clientPool.poll();
                    if (c != null) {
                        break;
                    } else {
                        if ((System.currentTimeMillis() - start) >= 1000) {
                            break;
                        }
                    }
                }
            }
        } catch (Exception e) {
            if (SoutUtil.isOpenSout())
                System.out.println(e.getMessage());
        }
        return c;
    }

    /**
     * @param client
     */
    public synchronized void returnClient(TDRpcClient client) {
        if (client == null) {
            return;
        }
        if (clientPool.size() < this.maxPoolSize) {
            this.clientPool.add(client);
        } else {
            client.close();
        }
    }

    /**
     * 
     */
    public void drop() {
        while (clientPool.size() > 0) {
            TDRpcClient client = clientPool.poll();
            client.close();
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
