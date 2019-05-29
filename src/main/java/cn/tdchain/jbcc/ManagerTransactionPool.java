/*
 * Copyright (c) 2017 Beijing Tiande Technology Co., Ltd.
 * All Rights Reserved.
 */
package cn.tdchain.jbcc;

import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Description: 事务管理池
 * @author xiaoming
 * 2019年4月18日
 */
class ManagerTransactionPool {
	public static String TRANSACTION_SPLIT_CHAR = "'";
	private static StringBuilder keys_srt = new StringBuilder();
	private static HashMap<String, Transaction> pool = new HashMap<String, Transaction>();
	
	static {
		//异步扫描pool里是否有过时事务？有则删除
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				while(true) {
					try {
						Iterator<String> nameI = pool.keySet().iterator();
						long current = System.currentTimeMillis();
						while(nameI.hasNext()) {
							String name = "";
							try {
								name = nameI.next();
							} catch (ConcurrentModificationException e) {
							}
							
							Transaction t = pool.get(name);
							if(t != null && t.getStopTime() <= current) {
								//过时事务
								removeTransaction(t);
							}
						}
						
						try {Thread.sleep(10);} catch (InterruptedException e) {}
					} catch (Exception e) {
						
					}
					
				}
			}
		}).start();
		
	}
	
	private static synchronized void removeTransaction(Transaction t) {
		if(t != null) {
			String keyName = t.getKeyName();
			Transaction die_t = pool.remove(keyName);
			
			if(die_t != null) {
				String[] keys = keyName.split(TRANSACTION_SPLIT_CHAR);
				for(int i = 0; keys != null && i < keys.length; i++) {
					String key = keys[i];
					keys_srt.delete(keys_srt.indexOf(key), keys_srt.indexOf(key) + key.length() + 1);
				}
			}
		}
	}
	
	/**
	 * Description: 判断key数组中 所有的key是否已经存在？全部不存在返回false/只少有一个key存在返回true
	 * @param keys
	 * @param t
	 * @return boolean
	 */
	private static synchronized boolean addKeyIsNotExist(String[] keys, Transaction t) {
		for(int i = 0; keys != null && i < keys.length; i++) {
			String key = keys[i];
			if(keys_srt.indexOf(key) != -1) {
				//key 已经被注入现在不能注入。
				return false;
			}
		}
		
		//可以注入事务
		t.setStartTime(System.currentTimeMillis());
		t.setStopTime(t.getStartTime() + (4000));//每一个key拥有3000最长生命周期时间
		keys_srt.append(t.getKeyName());
		pool.put(t.getKeyName(), t);
		return true;
	} 
	
	/**
	 * Description: 注册事务，成功返回true/失败返回false。 如果超过超时时间还没成功的话直接返回false
	 * @param t
	 * @param timeOut
	 * @return boolean
	 */
	public static boolean register(Transaction t, long timeOut) {
		boolean flag = false;
		long start = System.currentTimeMillis();
		
		String keyName = t.getKeyName();
		String[] keys = keyName.split(TRANSACTION_SPLIT_CHAR);
		while(true) {
			flag = addKeyIsNotExist(keys, t);
			if(flag) {
				//注入事务成功
				break;
			}
			
			if((System.currentTimeMillis() - start) > timeOut) {
				break;
			}
			
			try {Thread.sleep(1);} catch (InterruptedException e) {}
		}
		
		
		return flag;
	}
	
	public static void destroy(Transaction t) {
		removeTransaction(t);
	}
	
}
