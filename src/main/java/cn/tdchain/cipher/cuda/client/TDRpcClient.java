/*
 * Copyright (c) 2017 Beijing Tiande Technology Co., Ltd.
 * All Rights Reserved.
 */
package cn.tdchain.cipher.cuda.client;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * rpc client. 涓嶆槸绾跨▼瀹夊叏鐨勯摼鎺�
 * 
 * @version 2.0
 * @author Xiaoming 2017-12-14
 */
public class TDRpcClient {

    private String address;
    private int port;
    private int timeOut = 3000;
    private Socket socket = null;

    /**
     * Constructor.
     * 
     * @param address String
     * @param port int
     * @param timeOut int, default 3000
     * @throws IOException 
     */
    public TDRpcClient(String address, int port, int timeOut) throws IOException {
        this.address = address;
        this.port = port;
        this.timeOut = timeOut;

        /* Start to connection */
		newSocket();
    }

	private synchronized void newSocket() throws IOException {
		if (socket != null) {
			close();
		}
		if (socket == null) {
			socket = new Socket();
			socket.connect(new InetSocketAddress(this.address, this.port), timeOut);
			socket.setTcpNoDelay(true);
		}
	}

    /**
     * Send message.
     * 
     * @param msg message
     */
    public void send(String msg) {
        try {
        	 if (socket == null || socket.isClosed() || !socket.isConnected()) {
                 newSocket();
             }
        	 BufferedWriter oos = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
             oos.write(msg);
             oos.newLine();
             oos.flush();
        } catch (Exception e) {
            close();
        }
    }
    
    public void close() {
    	if(socket != null) {
    		try {
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
    		socket = null;
    	}
	}

	/**
     * Send a request and wait response.
     * 
     * @param msg String
     * @return response String
     */
    public String request(String msg) {
        String result = null;
        try {
        	 if (socket == null || socket.isClosed() || !socket.isConnected()) {
                 newSocket();
             }
            /** send request */
        	BufferedWriter oos = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            oos.write(msg);
            oos.newLine();
            oos.flush();

            /** wait response */
            BufferedReader bfr = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            result = bfr.readLine();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            // close con stream
        	close();
        } 
        return result;
    }
}
