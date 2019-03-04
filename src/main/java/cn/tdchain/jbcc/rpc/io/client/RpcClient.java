/*
 * Copyright (c) 2017 Beijing Tiande Technology Co., Ltd.
 * All Rights Reserved.
 */
package cn.tdchain.jbcc.rpc.io.client;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.SocketException;

import com.alibaba.fastjson.util.IOUtils;

import cn.tdchain.jbcc.rpc.io.eclc.EclcSocket;


/**
 * rpc client. 非线程安全的链接，底层采用对口令长连接协议。
 * 
 * @version 2.0
 * @author Xiaoming 2017-12-14
 */
public class RpcClient {
	private String serverPublicKey = null;
	private String clientPublicKey = null;
	private String token = "tiande@123456AbC";
    private String address;
    private int port;
    private int timeOut = 3000;
    private EclcSocket socket = null;

    /**
     * Constructor.
     * 
     * @param address String
     * @param port int
     * @param timeOut int, default 3000
     * @throws Exception 
     */
    public RpcClient(String address, int port, int timeOut, String token, String clientPublicKey) throws IOException,Exception {
        this.address = address;
        this.port = port;
        this.timeOut = timeOut;
        this.token = token;
        this.clientPublicKey = clientPublicKey;

        /* Start to connection */
        newSocket();
    }

	private synchronized void newSocket() throws IOException,Exception {
		if (socket != null) {
			close(null);
		}
		if (socket == null) {
			socket = new EclcSocket(this.clientPublicKey);
			socket.connect(new InetSocketAddress(this.address, this.port), timeOut, this.token);// 复杂密码提升网络安全
			socket.setTcpNoDelay(true);
			socket.setSoTimeout(timeOut);
			// 发送公钥、同时从server端获取公钥缓存到本地属性。
			this.serverPublicKey = socket.getServerPublicKey();
		}
	}

    /**
     * Send message.
     * 
     * @param msg message
     */
    public void send(String msg) throws  SocketException, IOException, Exception {
		if (socket == null || socket.isClosed() || !socket.isConnected()) {
			newSocket();
		}
		OutputStreamWriter os = new OutputStreamWriter(socket.getOutputStream());
		BufferedWriter oos = new BufferedWriter(os);
		oos.write(msg);
		oos.newLine();
		oos.flush();
    }
    
    /**
     * Send a request and wait response.
     * 
     * @param msg String
     * @return response String
     */
    public String sendAndReturn(String msg) throws  SocketException, IOException, Exception{
		if (socket == null || socket.isClosed() || !socket.isConnected()) {
			newSocket();
		}

		/** request */
		OutputStreamWriter os = new OutputStreamWriter(socket.getOutputStream());
		BufferedWriter oos = new BufferedWriter(os);
		oos.write(msg);
//		oos.newLine();
//		oos.flush();
//		oos.close();

		/** response 后续优化，如果服务端出现异常不响应会永远等待 */
		socket.setSoTimeout(10000);
		InputStreamReader read = new InputStreamReader(socket.getInputStream());
		BufferedReader bfr = new BufferedReader(read);
		oos.newLine();
		oos.flush();
		Thread.sleep(1);
		return bfr.readLine();
    }

    /**
     * Close.
     */
    public void close(Exception e) {
    	if(e != null) {
    		e.printStackTrace();
    	}
    	
        IOUtils.close(socket);
        socket = null;
    }

	public String getServerPublicKey() {
		return serverPublicKey;
	}

	/**
	 * @throws Exception 
	 * @throws IOException 
	 * @Description: 一直read line等待
	 * @return
	 * @throws
	 */
	public String readResult() throws IOException, Exception {
		if (socket == null || socket.isClosed() || !socket.isConnected()) {
			newSocket();
		}

		/** response 后续优化，如果服务端出现异常不响应会永远等待 */
		socket.setSoTimeout(0);
		InputStreamReader read = new InputStreamReader(socket.getInputStream());
		BufferedReader bfr = new BufferedReader(read);
		return bfr.readLine();
	}

    
}
