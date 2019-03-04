///*
// * Copyright (c) 2017 Beijing Tiande Technology Co., Ltd.
// * All Rights Reserved.
// */
//package cn.tdchain.cipher.sm.copy;
//
//import java.io.Serializable;
//import java.util.Base64;
//import java.util.Date;
//
//import cn.tdchain.cipher.utils.FileUtils;
//
//
///**
// * SM cerification.
// *
// * @version 2.0
// * @author Xiaoming 2017-05-03
// */
//public class SmCert implements Serializable {
//
//    private static final long serialVersionUID = 2906838343383310058L;
//
//    private byte[] publicKey;
//    private String signature;
//    private Date createTime;
//    private Date updateTime;
//    private String author;
//    private String summary;
//
//    /**
//     * Constructor.
//     */
//    public SmCert() {
//    }
//
//    /**
//     * Constructor.
//     * 
//     * @param publicKey byte[]
//     * @param privateKey byte[]
//     * @param storePath String
//     */
//    public SmCert(byte[] publicKey, byte[] privateKey, String storePath) {
//
//        this.publicKey = publicKey;
//        this.createTime = new Date();
//        this.updateTime = new Date();
//        this.author = "tdbc-sm";
//
//        initSummary();
//
//        try {
//            this.signature = Sm2Util.getInstance().sign("user", privateKey, this.summary);
//        } catch (Exception e) {
//            this.signature = null;
//        }
//        FileUtils.saveFile(storePath, this);
//    }
//
//    private void initSummary() {
//        String text = Base64.getEncoder().encodeToString(this.publicKey)
//                + this.createTime.getTime() + this.updateTime.getTime()
//                + this.author;
//        this.summary = Sm3Digest.hash(text);
//
//    }
//
//    public String getSignature() {
//        return signature;
//    }
//
//    public byte[] getPublicKey() {
//        return this.publicKey;
//    }
//}
