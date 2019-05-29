/*
 * Copyright (c) 2017 Beijing Tiande Technology Co., Ltd.
 * All Rights Reserved.
 */
package cn.tdchain.cipher.cuda.client;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Cipher client
 * @author xiaoming
 */
public class CUDACipherClient {
	private long WAIT_TIME = 1000;
	private String null_str = "null";
	private String padingChar = "0";
	private int blockSize = 16;
	private TDRpcClientPool pool = null;
	private ConcurrentLinkedQueue<String> dataQueue = new ConcurrentLinkedQueue<String>();
	private HashMap<String, String> resultMap = new HashMap<String, String>();
	
	
	public CUDACipherClient(String server, int port, int maxPoolSize) throws IOException {
		pool = new TDRpcClientPool(server, port, maxPoolSize);
		
		for(int i = 0; i < maxPoolSize; i++) {
			asynRsaDecrypt();
		}
	}
	
	private void asynRsaDecrypt() {
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				while(true) {
					try {
						boolean isFlag = false;
						
						// 1. param
						StringBuilder sb = new StringBuilder();
						while(true) {
							String data = dataQueue.poll();
							if(data != null) {
								sb.append(data + ",");//id;data;passwd,id;data;passwd
								isFlag = true;
							}else {
								break;
							}
						}
						
						// 2. send
						String value = null_str;
						if(isFlag) {
							sb.replace(sb.length() - 1, sb.length(), "");
							
							TDRpcClient client = pool.getClient();
							if(client == null) {
								throw new RuntimeException(" TDRpcClient is null.");
							}
							
							value = client.request("RSA_DECRYPT@" + sb.toString());
							pool.returnClient(client);
							
						}
						
						
						// 3. procces result
						if(!null_str.equals(value)) {
							String[] nodes = value.split(",");
							for(int i  = 0; (nodes != null) && (i < nodes.length); i++) {
								String node = nodes[i];
								String[] blockes = node.split(";");
								String id = blockes[0];
								String data = blockes[1];
								
								// save to result map
								resultMap.put(id, data);
							}
							
						}
						
						
						Thread.sleep(3);
					} catch (Exception e) {
						// TODO: handle exception
					}
				}
				
				
			}
		}).start();
	}
	
	
	/**
	 * AES encrypt
	 * @param text
	 * @param passwd
	 * @return String
	 */
	public String aes_encrypt(String text, String passwd) {
		TDRpcClient client = pool.getClient();
		if(client == null) {
			return null;
		}
		
		//padding
		try {
			int num = text.getBytes("UTF-8").length % blockSize;
			if(num > 0) {
				StringBuilder sb = new StringBuilder(text);
				int n = blockSize - num;
				for(int i = 0; i < n; i++) {
					sb.append(padingChar);
				}
				text = sb.toString();
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		
		String value = client.request("AES_ENCRYPT@" + text + "@" + passwd);
		pool.returnClient(client);
		
		return value;
	}
	
	/**
	 * AES decrypt
	 * @param cipherText
	 * @param passwd
	 * @return String
	 */
	public String aes_decrypt(String cipherText, String passwd) {
		TDRpcClient client = pool.getClient();
		if(client == null) {
			return null;
		}
		
		String value = client.request("AES_DECRYPT@" + cipherText + "@" + passwd);
		pool.returnClient(client);
		
		//un padding
		if(value != null && value.matches(".+0+")) {
			value = value.replaceAll("0+$", "");
		}
		
		return value;
	}
	
	/**
	 * sha 256 hash
	 * @param text
	 * @return String
	 */
	public String hash_256(String text) {
		TDRpcClient client = pool.getClient();
		if(client == null) {
			return null;
		}
		String value = client.request("SHASH@" + text);
		pool.returnClient(client);
		
		return value;
	}
	
	/**
	 * rsa 1024 key decrypt
	 * @param cipherText
	 * @param privateKey
	 * @return String
	 */
	public String rsa_decrypt(String cipherText, String privateKey) {
//		TDRpcClient client = pool.getClient();
//		if(client == null) {
//			return null;
//		}
//		
//		String value = client.request("RSA_DECRYPT@" + cipherText + "@" + privateKey);
//		pool.returnClient(client);
		
		// 1. put data to map,format id;data;passwd,id;data;passwd
        String id = UUID.randomUUID().toString();
        StringBuilder sb = new StringBuilder();
        sb.append(id).append(";").append(cipherText).append(";").append(privateKey);
        dataQueue.add(sb.toString());
        
		// 2. wait result
        String value = null;
		long start = System.currentTimeMillis();
		try {
			while ((System.currentTimeMillis() - start) <= WAIT_TIME) {
				value = resultMap.remove(id);
				if(value != null) {
					break;
				}
				
				Thread.sleep(3);
			}
		} catch (InterruptedException e) {
		}
		
		return value;
	}

}


