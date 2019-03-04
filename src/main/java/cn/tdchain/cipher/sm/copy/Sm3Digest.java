///*
// * Copyright (c) 2017 Beijing Tiande Technology Co., Ltd.
// * All Rights Reserved.
// */
//package cn.tdchain.cipher.sm.copy;
//
//import java.nio.charset.StandardCharsets;
//
//import cn.tdchain.cipher.utils.ByteUtils;
//
//
///**
// * SM3 Digest.
// *
// * @version 2.0
// * @author Xiaoming 2017-05-03
// */
//public class Sm3Digest {
//
//    private static final int BYTE_LENGTH = 32;
//
//    private static final int BLOCK_LENGTH = 64;
//
//    private static final int BUFFER_LENGTH = BLOCK_LENGTH * 1;
//
//    private byte[] xBuf = new byte[BUFFER_LENGTH];
//
//    private int xBufOff;
//
//    private byte[] v = Sm3Util.IV.clone();
//
//    private int cntBlock = 0;
//
//    /**
//     * Constructor.
//     */
//    public Sm3Digest() {
//    }
//
//    /**
//     * Constructor by copy.
//     * 
//     * @param source Sm3Digest
//     */
//    public Sm3Digest(Sm3Digest source) {
//        System.arraycopy(source.getXBuf(), 0, this.xBuf, 0,
//                source.getXBuf().length);
//        this.xBufOff = source.getXBufOff();
//        System.arraycopy(source.getV(), 0, this.v, 0, source.getV().length);
//    }
//
//    /**
//     * Encrption.
//     * 
//     * @param out byte[]
//     * @param outOff int
//     * @return BYTE_LENGTH
//     */
//    public int doFinal(byte[] out, int outOff) {
//        byte[] tmp = doFinal();
//        System.arraycopy(tmp, 0, out, 0, tmp.length);
//        return BYTE_LENGTH;
//    }
//
////    private void reset() {
////        xBufOff = 0;
////        cntBlock = 0;
////        v = Sm3Utils.IV.clone();
////    }
//
//    /**
//     * Update.
//     * 
//     * @param in byte[]
//     * @param inOff int
//     * @param len int
//     */
//    public void update(byte[] in, int inOff, int len) {
//        int partLen = BUFFER_LENGTH - xBufOff;
//        int inputLen = len;
//        int dPos = inOff;
//        if (partLen < inputLen) {
//            System.arraycopy(in, dPos, xBuf, xBufOff, partLen);
//            inputLen -= partLen;
//            dPos += partLen;
//            doUpdate();
//            while (inputLen > BUFFER_LENGTH) {
//                System.arraycopy(in, dPos, xBuf, 0, BUFFER_LENGTH);
//                inputLen -= BUFFER_LENGTH;
//                dPos += BUFFER_LENGTH;
//                doUpdate();
//            }
//        }
//
//        System.arraycopy(in, dPos, xBuf, xBufOff, inputLen);
//        xBufOff += inputLen;
//    }
//
//    private void doUpdate() {
//        byte[] newByte = new byte[BLOCK_LENGTH];
//        for (int i = 0; i < BUFFER_LENGTH; i += BLOCK_LENGTH) {
//            System.arraycopy(xBuf, i, newByte, 0, newByte.length);
//            doHash(newByte);
//        }
//        xBufOff = 0;
//    }
//
//    private void doHash(byte[] bytes) {
//        byte[] tmp = Sm3Util.cf(v, bytes);
//        System.arraycopy(tmp, 0, v, 0, v.length);
//        cntBlock++;
//    }
//
//    private byte[] doFinal() {
//        byte[] bytes = new byte[BLOCK_LENGTH];
//        byte[] buffer = new byte[xBufOff];
//        System.arraycopy(xBuf, 0, buffer, 0, buffer.length);
//        byte[] tmp = Sm3Util.padding(buffer, cntBlock);
//        for (int i = 0; i < tmp.length; i += BLOCK_LENGTH) {
//            System.arraycopy(tmp, i, bytes, 0, bytes.length);
//            doHash(bytes);
//        }
//        return v;
//    }
//
//    /**
//     * Update.
//     * 
//     * @param in byte
//     */
//    public void update(byte in) {
//        byte[] buffer = new byte[] { in };
//        update(buffer, 0, 1);
//    }
//
//    public int getDigestSize() {
//        return BYTE_LENGTH;
//    }
//
//    /**
//     * Hash.
//     * 
//     * @param text source string
//     * @return hex string
//     */
//    public static String hash(String text) {
//        byte[] md = new byte[32];
//        byte[] msg1;
//        Sm3Digest sm3 = new Sm3Digest();
//        msg1 = text.getBytes(StandardCharsets.UTF_8);
//        sm3.update(msg1, 0, msg1.length);
//        sm3.doFinal(md, 0);
//        return ByteUtils.byteToHex(md);
//    }
//
//    public byte[] getV() {
//        return v;
//    }
//
//    public void setV(byte[] v) {
//        this.v = v;
//    }
//
//    public byte[] getXBuf() {
//        return xBuf;
//    }
//
//    public int getXBufOff() {
//        return xBufOff;
//    }
//
//    public void setXBuf(byte[] xBuf) {
//        this.xBuf = xBuf;
//    }
//
//    public void setXBufOff(int xBufOff) {
//        this.xBufOff = xBufOff;
//    }
//
//}
