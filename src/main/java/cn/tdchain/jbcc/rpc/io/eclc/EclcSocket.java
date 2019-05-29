/*
 * Copyright (c) 2017 Beijing Tiande Technology Co., Ltd.
 * All Rights Reserved.
 */
package cn.tdchain.jbcc.rpc.io.eclc;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.UUID;

import cn.tdchain.cipher.rsa.AesUtil;

/**
对等口令长连接协议：拥有相同口令的A和B可以建立长连接的socket连接。
协议目的：防止恶意程序经过公网开放的ip和端口对区块链进行长连接攻击，也就意味着拥有口令的节点才能真正和其他节点进行建立网络连接。提升区块链网络安全能力/防止ddos攻击/提升网络传输效率。
 
协议步骤：
     1. A用户用口令“123”对明文“ABC”进行加密，并得到密文“@A”发送给B用户
     2. B用户收到A寄来的密文，同时进行解密得出明文“ABC”，此时B用户用自己id口令“123”对明文“BCD”进行加密得出密文“@B”
        B用户同时将“ABC”和“@B”返回给A用户
     3. A用户收到B的响应对比返回的明文是“ABC”，同时也要对B发过来的密文进行解密得出明文“BCD”，最后将明文“BCD”发送给B用户。
     4. B用户收到A用户发送过来的明文是“BCD”和自己在上步骤使用的明文是一致的，此时协议完成，A和B可以建立socket长连接。

 * @author xiaoming
 */
public class EclcSocket extends Socket {
	private int timeOut = 5000;
	private String serverPublicKey = null;
	private String clientPubliKey = null;
	private String connectionId;

	public EclcSocket(String localPubliKey,String connectionId) {
		this.connectionId = connectionId;
		this.clientPubliKey = localPubliKey;
	}

	public EclcSocket(InetAddress address, int port, InetAddress localAddr, int localPort, String passwd, String connectionId,String localPubliKey) throws IOException {
		super(address, port, localAddr, localPort);
		this.connectionId = connectionId;
		this.clientPubliKey = localPubliKey;
		/*
		 * eclc 口令验--》成功就允许建立长连接
		 *          --》失败就断开连接
		 * */
		eclc(passwd);
	}

	public EclcSocket(InetAddress address, int port, String passwd, String connectionId,String localPubliKey) throws IOException {
		super(address, port);
        this.connectionId = connectionId;
		this.clientPubliKey = localPubliKey;
		/*
		 * eclc 口令验--》成功就允许建立长连接
		 *          --》失败就断开连接
		 * */
		eclc(passwd);
	}

	public EclcSocket(String host, int port, InetAddress localAddr, int localPort, String passwd, String connectionId,String localPubliKey) throws IOException {
		super(host, port, localAddr, localPort);
        this.connectionId = connectionId;
		this.clientPubliKey = localPubliKey;
		/*
		 * eclc 口令验--》成功就允许建立长连接
		 *          --》失败就断开连接
		 * */
		eclc(passwd);
	}

	public EclcSocket(String host, int port, String passwd, String connectionId,String localPubliKey) throws UnknownHostException, IOException {
		super(host, port);
        this.connectionId = connectionId;
		this.clientPubliKey = localPubliKey;
		/*
		 * eclc 口令验--》成功就允许建立长连接
		 *          --》失败就断开连接
		 * */
		eclc(passwd);
	}



	public void connect(SocketAddress endpoint, int timeout, String passwd) throws IOException {
		super.connect(endpoint, timeout);
		eclc(passwd);
	}

	private void eclc(String passwd) {
		if(this.clientPubliKey == null || this.clientPubliKey.length() == 0) {
			throw new EclcException("null certificate.");
		}else {
			if(this.isConnected()) {
	        	try {
	        		this.setSoTimeout(timeOut);//超时没有回复抛出EclcException异常
	        		OutputStreamWriter os = new OutputStreamWriter(this.getOutputStream());
	            	BufferedWriter bwrite = new BufferedWriter(os);
	            	InputStreamReader ir = new InputStreamReader(this.getInputStream());
	            	BufferedReader bread = new BufferedReader(ir);

	            	// 1. 随机明文用passwd进行加密并发送出去。
	            	String data = UUID.randomUUID().toString();
	            	String data_chi = AesUtil.encrypt(data, passwd);
	            	bwrite.write(data_chi);
	            	bwrite.newLine();
					bwrite.flush();

	            	// 2. 等服务端解密后把明文返回  超时失败推出
//	            	System.out.println("data=" + data);
	            	String result_data = bread.readLine();
	            	if(result_data != null) {
	            		String[] v_array = result_data.split(";");
	            		if(v_array != null && v_array.length ==2) {
	            			String s_v_dada = v_array[0]; //
	            			String s_dada_chi = v_array[1]; //
	            			if(data.equals(s_v_dada)) {
	            				//解密server 发来的密文
	            				String s_dada= AesUtil.decrypt(s_dada_chi, passwd);
	            				if(s_dada != null) {
	            					//把解密出明文发送给服务的
	            					bwrite.write(s_dada);
	            					bwrite.newLine();
	            					bwrite.flush();
	            					//最后等待server端验证，如果通过则继续长连接否则会断开


	            					//给server 发送客户端公钥 和connectId
									String keyAndConnId = this.clientPubliKey+";"+this.connectionId;
	            					bwrite.write(keyAndConnId);
	            					bwrite.newLine();
	            					bwrite.flush();


	            					//接受server端发送过来的公钥
	            					String p_key = bread.readLine();
	            					if(p_key != null) {
	            					    this.serverPublicKey = p_key;
	            					}

	            					this.setSoTimeout(0);//恢复正常的无超时读写
	            				}else {
	            					throw new EclcException("Eclc  failed! passwd is not agreement.");
	            				}

	            			}else {
	            				throw new EclcException("Eclc  failed! passwd is not agreement.");
	            			}

	            		}else {
	            			throw new EclcException("Eclc  failed! eclc server result null v_array.");
	            		}

	            	}else {
	            		throw new EclcException("Eclc  failed! eclc server result null result_data.");
	            	}


				} catch (SocketException e) {
					e.printStackTrace();
					throw new EclcException("EclcServerSocket connection SocketException: " + e.getMessage());
				} catch (Exception e) {
					e.printStackTrace();
					throw new EclcException("EclcServerSocket connection Exception: " + e.getMessage());
				}


			}else {
				throw new EclcException("EclcServerSocket refuse connection.");
			}
		}

	}

	public String getServerPublicKey() {
		return serverPublicKey;
	}

	public String getClientPubliKey() {
		return clientPubliKey;
	}

	public void setClientPubliKey(String clientPubliKey) {
		this.clientPubliKey = clientPubliKey;
	}

	public void setServerPublicKey(String serverPublicKey) {
		this.serverPublicKey = serverPublicKey;
	}



}
