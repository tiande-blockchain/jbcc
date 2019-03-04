///*
// * Copyright (c) 2017 Beijing Tiande Technology Co., Ltd.
// * All Rights Reserved.
// */
//package cn.tdchain.cipher.sm.copy;
//
//import java.math.BigInteger;
//
//import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
//import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
//import org.bouncycastle.crypto.params.ECPublicKeyParameters;
//import org.bouncycastle.math.ec.ECPoint;
//
//import cn.tdchain.cipher.utils.ByteUtils;
//
//
///**
// * SM cipher.
// *
// * @version 2.0
// * @author Dengyq 2017-06-14
// */
//public class SmCipher {
//    private int ct;
//    private ECPoint p2;
//    private Sm3Digest sm3keybase;
//    private Sm3Digest sm3c3;
//    private byte[] key;
//    private byte keyOff;
//
//    /**
//     * Constructor.
//     */
//    public SmCipher() {
//        this.ct = 1;
//        this.key = new byte[32];
//        this.keyOff = 0;
//    }
//
//    private void reset() {
//        this.sm3keybase = new Sm3Digest();
//        this.sm3c3 = new Sm3Digest();
//
//        byte[] p = ByteUtils.get32Bytes(p2.getX().toBigInteger());
//        this.sm3keybase.update(p, 0, p.length);
//        this.sm3c3.update(p, 0, p.length);
//
//        p = ByteUtils.get32Bytes(p2.getY().toBigInteger());
//        this.sm3keybase.update(p, 0, p.length);
//        this.ct = 1;
//        nextKey();
//    }
//
//    private void nextKey() {
//        Sm3Digest sm3keycur = new Sm3Digest(this.sm3keybase);
//        sm3keycur.update((byte) (ct >> 24 & 0xff));
//        sm3keycur.update((byte) (ct >> 16 & 0xff));
//        sm3keycur.update((byte) (ct >> 8 & 0xff));
//        sm3keycur.update((byte) (ct & 0xff));
//        sm3keycur.doFinal(key, 0);
//        this.keyOff = 0;
//        this.ct++;
//    }
//
//    /**
//     * Init encryption.
//     * 
//     * @param key AsymmetricCipherKeyPair
//     * @param userKey ECPoint
//     * @return ECPoint
//     */
//    public ECPoint initEnc(AsymmetricCipherKeyPair key, ECPoint userKey) {
//        ECPrivateKeyParameters ecpriv = (ECPrivateKeyParameters) key
//                .getPrivate();
//        ECPublicKeyParameters ecpub = (ECPublicKeyParameters) key.getPublic();
//        BigInteger k = ecpriv.getD();
//        ECPoint c1 = ecpub.getQ();
//        this.p2 = userKey.multiply(k);
//        reset();
//        return c1;
//    }
//
//    /**
//     * Encryption.
//     * 
//     * @param data byte
//     */
//    public void encrypt(byte[] data) {
//        this.sm3c3.update(data, 0, data.length);
//        for (int i = 0; i < data.length; i++) {
//            if (keyOff == key.length) {
//                nextKey();
//            }
//            data[i] ^= key[keyOff++];
//        }
//    }
//
//    /**
//     * Init decryption.
//     * 
//     * @param userD BigInteger
//     * @param c1 ECPoint
//     */
//    public void initDec(BigInteger userD, ECPoint c1) {
//        this.p2 = c1.multiply(userD);
//        reset();
//    }
//
//    /**
//     * Decrypt.
//     * 
//     * @param data source data
//     */
//    public void decrypt(byte[] data) {
//        for (int i = 0; i < data.length; i++) {
//            if (keyOff == key.length) {
//                nextKey();
//            }
//            data[i] ^= key[keyOff++];
//        }
//
//        this.sm3c3.update(data, 0, data.length);
//    }
//
//    /**
//     * do final.
//     * 
//     * @param c3 source data
//     */
//    public void dofinal(byte[] c3) {
//        byte[] p = ByteUtils.get32Bytes(p2.getY().toBigInteger());
//        this.sm3c3.update(p, 0, p.length);
//        this.sm3c3.doFinal(c3, 0);
//        reset();
//    }
//}
