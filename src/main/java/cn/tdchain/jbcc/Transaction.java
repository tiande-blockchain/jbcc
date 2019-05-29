/*
 * Copyright (c) 2017 Beijing Tiande Technology Co., Ltd.
 * All Rights Reserved.
 */
package cn.tdchain.jbcc;


/**
 * Description: 事务对象
 * @author xiaoming
 * 2019年4月18日
 */
class Transaction {
	private String keyName = null; //key'key'
	private long startTime = 0;// 事务开始时间
	private long stopTime = 0;// 事务结束时间
	
	public Transaction(String[] keys) {
		if(keys == null || keys.length == 0) {
			return;
		}
		
		StringBuilder sb = new StringBuilder();
		for(String key:keys) {
			sb.append(key).append(ManagerTransactionPool.TRANSACTION_SPLIT_CHAR);
		}
		this.keyName = sb.toString();
		
	}
	
	
	public String getKeyName() {
		return keyName;
	}

	public long getStartTime() {
		return startTime;
	}
	public long getStopTime() {
		return stopTime;
	}


	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	public void setStopTime(long stopTime) {
		this.stopTime = stopTime;
	}
	
}
