/*
 * Copyright (c) 2017 Beijing Tiande Technology Co., Ltd.
 * All Rights Reserved.
 */
package cn.tdchain;

import cn.tdchain.cipher.rsa.Sha256Util;
import cn.tdchain.jbcc.BlockException;
import cn.tdchain.jbcc.MerkleUtil;
import cn.tdchain.jbcc.TransUtil;
import com.alibaba.fastjson.JSON;

import java.util.ArrayList;
import java.util.List;
/**
 * @Description: 天德区账本Block实体
 * @author xiaoming
 * @date:上午11:54:04
 */
public class Block <t extends Trans>{
    private Long height;//块高度
    private String hash;//块hash
    private String preHash; // 前一个block的hash
    private String merkleRoot;
    private String timestamp;
    private String sign;//签名
    
    /** 业务属性 */
    private int count; // 交易数量
    private List<t> trans; // 交易列表
    
	public Long getHeight() {
		return height;
	}
	public void setHeight(Long height) {
		this.height = height;
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
	public String getMerkleRoot() {
		return merkleRoot;
	}
	public void setMerkleRoot(String merkleRoot) {
		this.merkleRoot = merkleRoot;
	}
	public String getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}
	public int getCount() {
		return count;
	}
	public void setCount(int count) {
		this.count = count;
	}
	public List<t> getTrans() {
		return trans;
	}
	public void setTrans(List<t> trans) {
		this.trans = trans;
	}
	public String getSign() {
		return sign;
	}
	public void setSign(String sign) {
		this.sign = sign;
	}
	
	public String toJsonString() {
		return JSON.toJSONString(this);
	}
    
	/**
	 * block自身检查,验证hash、merkle_tree是否正确
	 * @Description: 
	 * @throws
	 */
	public void check() {
		// 1.验证 merkle_tree
		List<t> t_list = this.getTrans();
		if(t_list == null || t_list.size() == 0) {
			throw new BlockException("block list is null.");
		}
		
		List<String> blockHashList = new ArrayList<String>(this.getCount());
		for(t trans:t_list) {
			String hash = trans.getHash();
			if(hash != null && hash.length() > 0 && hash.contains("_")){
				try {
					hash = TransUtil.getHashByHeightHash(hash);
				} catch (Exception e) {
					throw new BlockException(e);
				}
			}
			blockHashList.add(hash);//累加全部交易hash
		}
		
		String merkleRoot = MerkleUtil.getMerkleRoot(blockHashList);
		if(getMerkleRoot() == null || !merkleRoot.equals(getMerkleRoot())) {
			throw new BlockException("block merkle root error.");
		}
		
		
		// 2.验证 hash
		String hash = Sha256Util.hash(getMerkleRoot() + getPreHash() + getTimestamp());
		if(getHash() == null || !getHash().equals(hash)) {
			throw new BlockException("block hash error.");
		}
		
	}
}
