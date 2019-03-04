/*
 * Copyright (c) 2017 Beijing Tiande Technology Co., Ltd.
 * All Rights Reserved.
 */
package cn.tdchain.cipher.cuda.client;

import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * rpc client pool. 绾跨▼瀹夊叏鐨勮繛鎺ュ璞℃睜锛屾敮鎸佸绾跨▼楂樺苟鍙戜娇鐢ㄥ悓涓�涓� client pool.
 * 
 * @version 2.0
 * @author Xiaoming 2017-12-14
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
     * @param port int
     * @throws IOException 
     */
    public TDRpcClientPool(String addres, int port) throws IOException {
        this(addres, port, 16, 1000);
    }

    /**
     * Constructor.
     * 
     * @param addres String
     * @param port int
     * @param maxPoolSize int
     * @throws IOException 
     */
    public TDRpcClientPool(String addres, int port, int maxPoolSize) throws IOException {
        this(addres, port, maxPoolSize, 1000);
    }

    /**
     * Constructor.
     * 
     * @param addres String
     * @param port int
     * @param maxPoolSize int
     * @param timeOut int, default 1000
     * @throws IOException 
     */
    public TDRpcClientPool(String addres, int port, int maxPoolSize,
            int timeOut) throws IOException {
        this.addres = addres;
        this.port = port;
        this.maxPoolSize = maxPoolSize;
        this.timeOut = timeOut;
        int minPoolSize = maxPoolSize / 10;
        if(minPoolSize > 1) {
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
     * 鎷胯蛋涓�涓摼鎺ュ璞�.
     * 
     * @return client
     */
    public TDRpcClient getClient() {
    	TDRpcClient c = null;
    	try {
        	long start = System.currentTimeMillis();
        	while(true) {
        		synchronized(this) {
        			c = this.clientPool.poll();
            		if(c != null) {
            			break;
            		}else {
            			if((System.currentTimeMillis() - start) >= 1000) {
            				break;
            			}
            		}
        		}
        	}
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return c;
    }

    /**
     * 褰掕繕閾炬帴瀵硅薄.
     * 
     * @param client TDRpcClient
     */
    public synchronized void returnClient(TDRpcClient client) {
        if (client == null) {
            return;
        }
        if (clientPool.size() < this.maxPoolSize) {
            this.clientPool.add(client);
        } else {
            // 閿�姣�
            client.close();
        }
    }

    /**
     * 閿�姣佽繛鎺ユ睜.
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
