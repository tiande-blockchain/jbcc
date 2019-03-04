/*
 * Copyright (c) 2017 Beijing Tiande Technology Co., Ltd.
 * All Rights Reserved.
 */
package cn.tdchain;

import com.alibaba.fastjson.JSON;

import cn.tdchain.cipher.utils.HashCheckUtil;
import cn.tdchain.jbcc.DateUtils;
import cn.tdchain.jbcc.SQLCheckUtil;
import cn.tdchain.jbcc.TransInfoException;
import cn.tdchain.jbcc.TransUtil;

/**
 * @Description: 天德区块链交易实体
 * @author xiaoming
 * @date: 2018年10月29日 下午6:44:56
 */
public class Trans {
	protected String timestamp;//交易时间

	protected String preHash="null";//key维度的前一个交易的hash
	
	protected String hash="null";//交易摘要

	protected String blockHash;//对应block hash
	
	protected String key;//交易维度
    
	protected String data;//其他交易数据，json object 的字符串结构
	
	//交易状态和描述
	protected String type = "null";//交易类型长度不能超过45
	protected String connectionId;// 发送者、或者是connection_id
	protected TransStatus status = TransStatus.prep;//默认时准备状态
	protected String msg;//当交易错误时，相关错误信息会被存入此字段中。

	private Long version;
	private Long height;

	public Long getVersion() {
		return version;
	}

	public void setVersion(Long version) {
		this.version = version;
	}

	public Long getHeight() {
		return height;
	}

	public void setHeight(Long height) {
		this.height = height;
	}

	
	public String getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}

	public String getHash() {
		return hash;
	}

	public void setHash(String hash) {
		this.hash = hash;
	}

	public String getPreHash() {
		return preHash;
	}

	public void setPreHash(String preHash) {
		this.preHash = preHash;
	}

	public String getBlockHash() {
		return blockHash;
	}

	public void setBlockHash(String blockHash) {
		this.blockHash = blockHash;
	}
	
	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public TransStatus getStatus() {
		return status;
	}

	public void setStatus(TransStatus status) {
		this.status = status;
	}
	
	public String getConnectionId() {
		return connectionId;
	}

	public void setConnectionId(String connectionId) {
		this.connectionId = connectionId;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public void upHash() {
		//验证key不能为null
		if(this.key == null || this.key.trim().length() == 0 || this.key.trim().length() > 255) {
			throw new TransInfoException("key is null or too long.");
		}
		
		this.setTimestamp(DateUtils.getCurrentTime());
		
		if(this.hash != null && this.hash.length() > 0 && !this.hash.equals("null")) {
			this.preHash = this.hash;//当前交易hash会变成历史交易hash
		}
		
		this.hash = computHash(this);
	}
	
	/**
	 * @Description: 计算出交易hash
	 * @param t
	 * @return
	 * @throws
	 */
	public String computHash(Trans t) {
		return TransUtil.getTransHash(t);
	}
	
	/**
	 * 验证自己属性
	 * @Description: 
	 * @throws
	 */
	public void check() {
		// 交易属性验证
		if(this.getKey() == null || this.getKey().length() == 0 || this.getKey().length() > 255) {
			throw new TransInfoException("key is null or too long, max size is 255.");
		}
		
		// 去掉交易空格
        this.setKey(this.getKey().trim());
		
		//key不能有特殊字符
		if(HashCheckUtil.illegalCharacterCheck(this.key)) {
			throw new TransInfoException("key have Illegal character.");
		}
		
		//type字段不能有特殊字符
		if(HashCheckUtil.illegalCharacterCheck(this.type) || (this.type != null && this.type.length() > 45)) {
			throw new TransInfoException("type have Illegal character.");
		}
		
		if(SQLCheckUtil.checkSQLError(this.getKey())) {
			throw new TransInfoException("key have Illegal character.");
		}
		
		if(this.getData() == null || this.getData().trim().length() == 0 || this.getData().trim().getBytes().length > 65535) {
			throw new TransInfoException("data is null or too long,max byte size is 65535.");
		}
		
		//data不能有特殊字符
		if(HashCheckUtil.illegalCharacterCheck(this.data)) {
			throw new TransInfoException("data have Illegal character.");
		}
		
		if(this.getHash() == null || this.getHash().trim().length() != 64) {
			this.upHash();
		}
		
		//hash验证
		if(!HashCheckUtil.hashCheck(this.hash)) {
			throw new TransInfoException("hash error:" + this.hash);
		}
		
		//pre hash验证，要么等于"null"要么就是正常的hash字符串
		if(!HashCheckUtil.hashCheck(this.preHash) && !"null".equals(this.preHash)) {
			throw new TransInfoException("pre hash error:" + this.preHash);
		}
		
		
		//检查超交易生成时间，超过1秒的交易可能存在问题.
		checkStartTime();
		
	}
	/**
	 * @Description: 检查交易是否过时？不能超过6秒之后再提交交易。
	 * @throws
	 */
	protected void checkStartTime() {
		Long differ_time = (System.currentTimeMillis() - DateUtils.getTime(this.getTimestamp()));
        if((differ_time > 6000) || differ_time < -6000) {
			//过时交易或者交易时间戳超前
        	throw new TransInfoException("the trans out of time or timestamp is error.");
		}
	}

	/**
	 * 交易状态
	 * @author xiaoming
	 * @date: 2018年11月16日 下午6:21:22
	 */
	public enum TransStatus {
		prep,success,failed
	}

	public String toJsonString() {
		return JSON.toJSONString(this);
	}
	
//	public static void main(String[] args) {
//		Trans t = new Trans();
//		t.setKey("a");
//		t.setData("\\");
//		System.out.println(t.toJsonString());
//		System.out.println(t.getData().matches(".*['||;||\\\\||\n||\r].*"));
//		
//		System.out.println(t.getKey().matches(".*['||;||\\\\||\n||\r].*"));
//	}
	
}
