///*
// * Copyright (c) 2017 Beijing Tiande Technology Co., Ltd.
// * All Rights Reserved.
// */
//package cn.tdchain.cipher.sm.copy;
//
///**
// * SM4 Context.
// *
// * @version 2.0
// * @author Xiaoming 2017-05-03
// */
//public class Sm4Context {
//    
//    private static final int SM4_ENCRYPT = 1;
//    
//    private static final int SM4_DECRYPT = 0;
//
//    private int mode;
//
//    private long[] sk;
//
//    private boolean isPadding;
//
//    /**
//     * Constructor.
//     */
//    public Sm4Context() {
//        this.mode = SM4_ENCRYPT;
//        this.isPadding = true;
//        this.sk = new long[32];
//    }
//
//    /**
//     * Set mode to encrption.
//     */
//    public void setEncryptMode() {
//        this.mode = SM4_ENCRYPT;
//    }
//
//    /**
//     * Set mode to decrption.
//     */
//    public void setDecryptMode() {
//        this.mode = SM4_DECRYPT;
//    }
//
//    /**
//     * Is encryption mode.
//     * 
//     * @return true if is encryption mode
//     */
//    public boolean isEncryptMode() {
//        return this.mode == SM4_ENCRYPT;
//    }
//
//    /**
//     * Is decryption mode.
//     * 
//     * @return true if is decryption mode.
//     */
//    public boolean isDecryptMode() {
//        return this.mode == SM4_DECRYPT;
//    }
//
//    public long[] getSk() {
//        return sk;
//    }
//
//    public void setSk(long[] sk) {
//        this.sk = sk;
//    }
//
//    public boolean isPadding() {
//        return isPadding;
//    }
//
//    public void setPadding(boolean isPadding) {
//        this.isPadding = isPadding;
//    }
//
//}
