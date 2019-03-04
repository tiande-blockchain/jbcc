///*
// * Copyright (c) 2017 Beijing Tiande Technology Co., Ltd.
// * All Rights Reserved.
// */
//package cn.tdchain.cipher.sm.copy;
//
//import java.io.Serializable;
//import java.util.Date;
//
///**
// * SM Key Store.
// *
// * @version 2.0
// * @author Houmj 2017-10-12
// */
//public class SmKeyStore implements Serializable {
//
//    private static final long serialVersionUID = -5243433312941864671L;
//    private byte[] privateKey;
//    private byte[] publicKey;
//    private String signature;
//    private Date createTime;
//    private Date updateTime;
//    private String author;
//    private String summary;
//    private SmCert cert;
//
//    /**
//     * Constructor.
//     */
//    public SmKeyStore() {
//    }
//
//    public byte[] getPrivateKey() {
//        return privateKey;
//    }
//
//    public void setPrivateKey(byte[] privateKey) {
//        this.privateKey = privateKey;
//    }
//
//    public byte[] getPublicKey() {
//		return publicKey;
//	}
//
//	public void setPublicKey(byte[] publicKey) {
//		this.publicKey = publicKey;
//	}
//
//	public String getSignature() {
//        return signature;
//    }
//
//    public void setSignature(String signature) {
//        this.signature = signature;
//    }
//
//    public Date getCreateTime() {
//        return createTime;
//    }
//
//    public void setCreateTime(Date createTime) {
//        this.createTime = createTime;
//    }
//
//    public Date getUpdateTime() {
//        return updateTime;
//    }
//
//    public void setUpdateTime(Date updateTime) {
//        this.updateTime = updateTime;
//    }
//
//    public String getAuthor() {
//        return author;
//    }
//
//    public void setAuthor(String author) {
//        this.author = author;
//    }
//
//    public String getSummary() {
//        return summary;
//    }
//
//    public void setSummary(String summary) {
//        this.summary = summary;
//    }
//
//    public SmCert getCert() {
//        return cert;
//    }
//
//    public void setCert(SmCert cert) {
//        this.cert = cert;
//    }
//
//}
