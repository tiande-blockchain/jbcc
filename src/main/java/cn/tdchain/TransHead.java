/*
 * Copyright (c) 2017 Beijing Tiande Technology Co., Ltd.
 * All Rights Reserved.
 */
package cn.tdchain;

import com.alibaba.fastjson.JSON;

import cn.tdchain.Trans.TransStatus;

/**
 * Description: 描述交易基本信息
 * @author xiaoming
 * 2019年4月9日
 */
public class TransHead {
	protected String timestamp;//交易时间

    protected String preHash = "";//key维度的前一个交易的hash

    protected String hash = "null";//交易摘要

    protected String blockHash;//对应block hash

    protected String key;//交易维度
    
    
    //交易状态和描述
    protected String type = "";//交易类型长度不能超过45
    protected String connectionId;// 发送者、或者是connection_id
    protected TransStatus status = TransStatus.prep;//默认时准备状态
//    protected String msg;//当交易错误时，相关错误信息会被存入此字段中。
    
    protected Long version;
    protected Long height;
    
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

//    public String getMsg() {
//        return msg;
//    }
//
//    public void setMsg(String msg) {
//        this.msg = msg;
//    }

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
    
    public String toJsonString() {
        return JSON.toJSONString(this);
    }
    
	@Override
	public String toString() {
		return toJsonString();
	}

	public void upHash() {}

	public void check() {}
	
	public void check(boolean enableVerifySensitiveWorks) {}
	
}
