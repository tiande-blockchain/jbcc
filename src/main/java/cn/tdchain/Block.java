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
 * Description: 天德区账本Block实体
 * @author xiaoming
 * 2019年4月18日
 */
public class Block<T extends Trans> {
    protected Long height;//块高度
    protected String hash;//块hash
    protected String preHash; // 前一个block的hash
    protected String merkleRoot;
    protected String timestamp;
    protected String sign;//签名

    /**
     * Description: 业务属性
     */
    protected int count; // 交易数量
    protected List<T> trans; // 交易列表

    /**
     * 创建者
     */
    protected String createrName;
    /**
     * 创建者ID
     */
    protected String createrId;

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

    public List<T> getTrans() {
        return trans;
    }

    public void setTrans(List<T> trans) {
        this.trans = trans;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    public String getCreaterName() {
        return createrName;
    }

    public void setCreaterName(String createrName) {
        this.createrName = createrName;
    }

    public String getCreaterId() {
        return createrId;
    }

    public void setCreaterId(String createrId) {
        this.createrId = createrId;
    }

    public String toJsonString() {
        return JSON.toJSONString(this);
    }

    /**
     * block自身检查,验证hash、merkle_tree是否正确
     */
    public void check() {
        // 1.验证 merkle_tree
        List<T> t_list = this.getTrans();
        if (t_list == null || t_list.size() == 0) {
            throw new BlockException("block list is null.");
        }

        List<String> blockHashList = new ArrayList<>(this.getCount());
        for (T trans : t_list) {
            String hash = TransUtil.getHash(trans);
            blockHashList.add(hash);//累加全部交易hash
        }

        String merkleRoot = MerkleUtil.getMerkleRoot(blockHashList);
        if (getMerkleRoot() == null || !merkleRoot.equals(getMerkleRoot())) {
            throw new BlockException("block merkle root error.");
        }

        // 2.验证 hash
        String hash = Sha256Util.hash(getMerkleRoot() + getPreHash() + getTimestamp());
        if (getHash() == null || !getHash().equals(hash)) {
            throw new BlockException("block hash error.");
        }
    }
}
