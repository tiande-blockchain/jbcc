///*
// * Copyright (c) 2017 Beijing Tiande Technology Co., Ltd.
// * All Rights Reserved.
// */
//package cn.tdchain.cipher.sm.copy;
//
//import java.math.BigInteger;
//
//import org.bouncycastle.math.ec.ECPoint;
//
///**
// * SM2 Result.
// *
// * @version 2.0
// * @author Xiaoming 2017-05-03
// */
//public class Sm2Result {
//
//    private BigInteger lowerR;
//    private BigInteger s;
//    private BigInteger upperR;
//
//    private byte[] sa;
//    private byte[] sb;
//    private byte[] s1;
//    private byte[] s2;
//
//    private ECPoint keyra;
//    private ECPoint keyrb;
//    
//    /**
//     * Constructor.
//     */
//    public Sm2Result() {
//    }
//
//    public BigInteger getLowerR() {
//        return lowerR;
//    }
//
//    public void setLowerR(BigInteger r) {
//        this.lowerR = r;
//    }
//
//    public BigInteger getS() {
//        return s;
//    }
//
//    public void setS(BigInteger s) {
//        this.s = s;
//    }
//
//    public BigInteger getUpperR() {
//        return upperR;
//    }
//
//    public void setUpperR(BigInteger r) {
//        upperR = r;
//    }
//
//    public byte[] getSa() {
//        return sa;
//    }
//
//    public void setSa(byte[] sa) {
//        this.sa = sa;
//    }
//
//    public byte[] getSb() {
//        return sb;
//    }
//
//    public void setSb(byte[] sb) {
//        this.sb = sb;
//    }
//
//    public byte[] getS1() {
//        return s1;
//    }
//
//    public void setS1(byte[] s1) {
//        this.s1 = s1;
//    }
//
//    public byte[] getS2() {
//        return s2;
//    }
//
//    public void setS2(byte[] s2) {
//        this.s2 = s2;
//    }
//
//    public ECPoint getKeyra() {
//        return keyra;
//    }
//
//    public void setKeyra(ECPoint keyra) {
//        this.keyra = keyra;
//    }
//
//    public ECPoint getKeyrb() {
//        return keyrb;
//    }
//
//    public void setKeyrb(ECPoint keyrb) {
//        this.keyrb = keyrb;
//    }
//
//}
