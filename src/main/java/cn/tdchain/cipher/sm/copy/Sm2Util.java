///*
// * Copyright (c) 2017 Beijing Tiande Technology Co., Ltd.
// * All Rights Reserved.
// */
//package cn.tdchain.cipher.sm.copy;
//
//import java.io.ByteArrayInputStream;
//import java.io.ByteArrayOutputStream;
//import java.io.IOException;
//import java.math.BigInteger;
//import java.nio.charset.StandardCharsets;
//import java.security.SecureRandom;
//import java.util.Base64;
//import java.util.Enumeration;
//
//import org.bouncycastle.asn1.ASN1EncodableVector;
//import org.bouncycastle.asn1.ASN1InputStream;
//import org.bouncycastle.asn1.ASN1Sequence;
//import org.bouncycastle.asn1.DERInteger;
//import org.bouncycastle.asn1.DERObject;
//import org.bouncycastle.asn1.DEROctetString;
//import org.bouncycastle.asn1.DEROutputStream;
//import org.bouncycastle.asn1.DERSequence;
//import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
//import org.bouncycastle.crypto.generators.ECKeyPairGenerator;
//import org.bouncycastle.crypto.params.ECDomainParameters;
//import org.bouncycastle.crypto.params.ECKeyGenerationParameters;
//import org.bouncycastle.math.ec.ECCurve;
//import org.bouncycastle.math.ec.ECFieldElement;
//import org.bouncycastle.math.ec.ECFieldElement.Fp;
//
//import cn.tdchain.cipher.utils.ByteUtils;
//
//import org.bouncycastle.math.ec.ECPoint;
//
//import com.alibaba.fastjson.util.IOUtils;
//
///**
// * SM2 Utility.
// *
// * @version 2.0
// * @author Xiaoming 2017-05-03
// */
//public class Sm2Util {
//
//
//    private static final String[] ECC_PARAM = {
//        "8542D69E4C044F18E8B92435BF6FF7DE457283915C45517D722EDB8B08F1DFC3",
//        "787968B4FA32C3FD2417842E73BBFEFF2F3C848B6831D7E0EC65228B3937E498",
//        "63E4C6D3B23B0C849CF84241484BFE48F61D59A5B16BA06E6E12D1DA27C5249A",
//        "8542D69E4C044F18E8B92435BF6FF7DD297720630485628D5AE74EE7C32E79B7",
//        "421DEBD61B62EAB6746434EBC3CC315E32220B3BADD50BDC4C4E6C147FEDD43D",
//        "0680512BCBB42C07D47349D2153B70C4E5D7FDFCBFA36EA1A85841B9E46E09A2" };
//
//    private static Sm2Util instance;
//    private final BigInteger eccP;
//    private final BigInteger eccA;
//    private final BigInteger eccB;
//    private final BigInteger eccN;
//    private final BigInteger eccGx;
//    private final BigInteger eccGy;
//    private final ECCurve eccCurve;
//    private final ECPoint eccPointG;
//    private final ECDomainParameters eccBcSpec;
//    private final ECKeyPairGenerator eccKeypairGenerator;
//    private final ECFieldElement eccGxFieldelement;
//    private final ECFieldElement eccGyFfieldelement;
//
//    /**
//     * Default constructor.
//     */
//    public Sm2Util() {
//        this.eccP = new BigInteger(ECC_PARAM[0], 16);
//        this.eccA = new BigInteger(ECC_PARAM[1], 16);
//        this.eccB = new BigInteger(ECC_PARAM[2], 16);
//        this.eccN = new BigInteger(ECC_PARAM[3], 16);
//        this.eccGx = new BigInteger(ECC_PARAM[4], 16);
//        this.eccGy = new BigInteger(ECC_PARAM[5], 16);
//
//        this.eccGxFieldelement = new Fp(this.eccP, this.eccGx);
//        this.eccGyFfieldelement = new Fp(this.eccP, this.eccGy);
//
//        this.eccCurve = new ECCurve.Fp(this.eccP, this.eccA, this.eccB);
//        this.eccPointG = new ECPoint.Fp(this.eccCurve, this.eccGxFieldelement,
//                this.eccGyFfieldelement);
//
//        this.eccBcSpec = new ECDomainParameters(this.eccCurve, this.eccPointG,
//                this.eccN);
//
//        ECKeyGenerationParameters eccEcgenparam;
//        eccEcgenparam = new ECKeyGenerationParameters(this.eccBcSpec,
//                new SecureRandom());
//
//        this.eccKeypairGenerator = new ECKeyPairGenerator();
//        this.eccKeypairGenerator.init(eccEcgenparam);
//    }
//
//    /**
//     * Get new instance.
//     * 
//     * @return SM2 instanceO
//     */
//    public static Sm2Util getInstance() {
//        if (instance == null) {
//            instance = new Sm2Util();
//        }
//        return instance;
//    }
//
//    private byte[] sm2GetZ(byte[] userId, ECPoint userKey) {
//        Sm3Digest sm3 = new Sm3Digest();
//
//        int len = userId.length * 8;
//        sm3.update((byte) (len >> 8 & 0xFF));
//        sm3.update((byte) (len & 0xFF));
//        sm3.update(userId, 0, userId.length);
//
//        update(sm3, eccA);
//        update(sm3, eccB);
//        update(sm3, eccGx);
//        update(sm3, eccGy);
//        update(sm3, userKey.getX().toBigInteger());
//        update(sm3, userKey.getY().toBigInteger());
//
//        byte[] md = new byte[sm3.getDigestSize()];
//        sm3.doFinal(md, 0);
//        return md;
//    }
//
//    private void update(Sm3Digest sm3, BigInteger src) {
//        byte[] p = ByteUtils.get32Bytes(src);
//        sm3.update(p, 0, p.length);
//    }
//
//    /**
//     * Generate keypair.
//     * 
//     * @return keypair
//     */
//    public AsymmetricCipherKeyPair generateKeyPair() {
//        return eccKeypairGenerator.generateKeyPair();
//    }
//
//    /**
//     * Sign.
//     * 
//     * @param user user id
//     * @param privateKey private key
//     * @param data source data
//     * @return signed data
//     */
//    public String sign(String user, byte[] privateKey, String data) {
//        if (user == null || user.length() == 0 || ByteUtils.isEmpty(privateKey)) {
//            return data;
//        }
//        byte[] userId = user.getBytes(StandardCharsets.UTF_8);
//
//        byte[] sourceData = data.getBytes(StandardCharsets.UTF_8);
//
//        if (ByteUtils.isEmpty(userId) || ByteUtils.isEmpty(sourceData)) {
//            return data;
//        }
//
//        BigInteger userD = new BigInteger(privateKey);
//        ECPoint userKey = eccPointG.multiply(userD);
//
//        Sm3Digest sm3 = new Sm3Digest();
//        byte[] z = sm2GetZ(userId, userKey);
//
//        sm3.update(z, 0, z.length);
//        sm3.update(sourceData, 0, sourceData.length);
//        byte[] md = new byte[32];
//        sm3.doFinal(md, 0);
//
//        Sm2Result sm2Result = new Sm2Result();
//        sm2Sign(md, userD, userKey, sm2Result);
//
//        DERInteger dR = new DERInteger(sm2Result.getLowerR());
//        DERInteger dS = new DERInteger(sm2Result.getS());
//        ASN1EncodableVector v2 = new ASN1EncodableVector();
//        v2.add(dR);
//        v2.add(dS);
//        DERObject sign = new DERSequence(v2);
//        byte[] signdata = sign.getDEREncoded();
//        return Base64.getEncoder().encodeToString(signdata);
//    }
//
//    private void sm2Sign(byte[] md, BigInteger userD, ECPoint userKey,
//                         Sm2Result sm2Result) {
//        BigInteger e = new BigInteger(1, md);
//        BigInteger k = null;
//        ECPoint kp = null;
//        BigInteger r = null;
//        BigInteger s = null;
//        do {
//            do {
//
//                String kS = "6CB28D99385C175C94F94E934817663FC176D925DD72B727260DBAAE1FB2F96F";
//                k = new BigInteger(kS, 16);
//                kp = this.eccPointG.multiply(k);
//
//                r = e.add(kp.getX().toBigInteger());
//                r = r.mod(eccN);
//            } while (r.equals(BigInteger.ZERO) || r.add(k).equals(eccN));
//
//            // (1 + dA)~-1
//            BigInteger daAddOne = userD.add(BigInteger.ONE);
//            daAddOne = daAddOne.modInverse(eccN);
//
//            // s
//            s = r.multiply(userD);
//            s = k.subtract(s).mod(eccN);
//            s = daAddOne.multiply(s).mod(eccN);
//        } while (s.equals(BigInteger.ZERO));
//
//        sm2Result.setLowerR(r);
//        sm2Result.setS(s);
//    }
//
//    /**
//     * Encrypt by public key.
//     * 
//     * @param pubKey public key string
//     * @param dataStr original data string
//     * @return encrypted data
//     */
//    public String encrypt(String pubKey, String dataStr) {
//        if (pubKey == null || pubKey.length() == 0) {
//            return dataStr;
//        }
//        byte[] publicKey = Base64.getDecoder().decode(pubKey);
//        byte[] data = dataStr.getBytes(StandardCharsets.UTF_8);
//
//        if (ByteUtils.isEmpty(publicKey) || ByteUtils.isEmpty(data)) {
//            return dataStr;
//        }
//
//        byte[] source = new byte[data.length];
//        System.arraycopy(data, 0, source, 0, data.length);
//
//        SmCipher cipher = new SmCipher();
//        ECPoint userKey = eccCurve.decodePoint(publicKey);
//
//        ECPoint c1 = cipher.initEnc(generateKeyPair(), userKey);
//        cipher.encrypt(source);
//        byte[] c3 = new byte[32];
//        cipher.dofinal(c3);
//
//        DERInteger x = new DERInteger(c1.getX().toBigInteger());
//        DERInteger y = new DERInteger(c1.getY().toBigInteger());
//        DEROctetString derDig = new DEROctetString(c3);
//        DEROctetString derEnc = new DEROctetString(source);
//        ASN1EncodableVector v = new ASN1EncodableVector();
//        v.add(x);
//        v.add(y);
//        v.add(derDig);
//        v.add(derEnc);
//
//        DERSequence seq = new DERSequence(v);
//        ByteArrayOutputStream baos = null;
//        DEROutputStream dos = null;
//        byte[] res = null;
//        try {
//            baos = new ByteArrayOutputStream();
//            dos = new DEROutputStream(baos);
//            dos.writeObject(seq);
//            res = baos.toByteArray();
//        } catch (Exception e) {
//        	e.printStackTrace();
//            res = source;
//        } finally {
//            IOUtils.close(dos);
//            IOUtils.close(baos);
//        }
//
//        return Base64.getEncoder().encodeToString(res);
//    }
//
//    /**
//     * Decrypt by private key.
//     * 
//     * @param priKey private key string
//     * @param dataStr original data string
//     * @return decrypted data
//     */
//    public String decrypt(String priKey, String dataStr) {
//    	try {
//    		if (dataStr == null || dataStr.length() == 0) {
//                return null;
//            }
//            if (priKey == null || priKey.length() == 0) {
//                return dataStr;
//            }
//
//            byte[] privateKey;
//            byte[] encryptedData;
//            try {
//                privateKey = Base64.getDecoder().decode(priKey);
//                encryptedData = Base64.getDecoder().decode(dataStr);
//            } catch (Exception e) {
//                e.printStackTrace();
//                return dataStr;
//            }
//            if (ByteUtils.isEmpty(privateKey) || ByteUtils.isEmpty(encryptedData)) {
//                return dataStr;
//            }
//
//            byte[] enc = new byte[encryptedData.length];
//            System.arraycopy(encryptedData, 0, enc, 0, encryptedData.length);
//
//            BigInteger userD = new BigInteger(1, privateKey);
//
//            ByteArrayInputStream bis = null;
//            ASN1InputStream dis = null;
//            try {
//                bis = new ByteArrayInputStream(enc);
//                dis = new ASN1InputStream(bis);
//                DERObject derObj = dis.readObject();
//                ASN1Sequence asn1 = (ASN1Sequence) derObj;
//                DERInteger x = (DERInteger) asn1.getObjectAt(0);
//                DERInteger y = (DERInteger) asn1.getObjectAt(1);
//                ECPoint c1 = eccCurve.createPoint(x.getValue(), y.getValue(), true);
//                SmCipher cipher = new SmCipher();
//                cipher.initDec(userD, c1);
//                DEROctetString data = (DEROctetString) asn1.getObjectAt(3);
//                enc = data.getOctets();
//                cipher.decrypt(enc);
//                byte[] c3 = new byte[32];
//                cipher.dofinal(c3);
//            } catch (Exception e) {
//                e.printStackTrace();
//                enc = encryptedData;
//            } finally {
//                IOUtils.close(dis);
//                IOUtils.close(bis);
//            }
//
//            return new String(enc, StandardCharsets.UTF_8);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//        
//    	return null;
//    }
//
//    /**
//     * 公钥解签.
//     * 
//     * @param user String
//     * @param publicKey byte[]
//     * @param dataStr String
//     * @param signText String
//     * @return verify result
//     */
//    @SuppressWarnings("unchecked")
//    public boolean verify(String user, byte[] publicKey, String dataStr,
//                          String signText) {
//        if (user == null || user.length() == 0 || signText == null || signText.length() == 0
//                || ByteUtils.isEmpty(publicKey)) {
//            return false;
//        }
//        byte[] userId = user.getBytes(StandardCharsets.UTF_8);
//
//        byte[] sourceData = dataStr.getBytes(StandardCharsets.UTF_8);
//        byte[] signData = Base64.getDecoder().decode(signText);
//        if (ByteUtils.isEmpty(userId) || ByteUtils.isEmpty(sourceData)
//                || ByteUtils.isEmpty(signData)) {
//            return false;
//        }
//
//        ECPoint userKey = eccCurve.decodePoint(publicKey);
//
//        Sm3Digest sm3 = new Sm3Digest();
//        byte[] z = sm2GetZ(userId, userKey);
//        sm3.update(z, 0, z.length);
//        sm3.update(sourceData, 0, sourceData.length);
//        byte[] md = new byte[32];
//        sm3.doFinal(md, 0);
//
//        ByteArrayInputStream bis = null;
//        ASN1InputStream dis = null;
//        boolean flag = false;
//        try {
//            bis = new ByteArrayInputStream(signData);
//            dis = new ASN1InputStream(bis);
//            DERObject derObj = dis.readObject();
//            Enumeration<DERInteger> e = ((ASN1Sequence) derObj).getObjects();
//            BigInteger r = ((DERInteger) e.nextElement()).getValue();
//            BigInteger s = ((DERInteger) e.nextElement()).getValue();
//            Sm2Result sm2Result = new Sm2Result();
//            sm2Result.setLowerR(r);
//            sm2Result.setS(s);
//
//            sm2Verify(md, userKey, sm2Result.getLowerR(), sm2Result.getS(),
//                    sm2Result);
//            flag = sm2Result.getLowerR().equals(sm2Result.getUpperR());
//        } catch (IOException e1) {
//            e1.printStackTrace();
//            flag = false;
//        } finally {
//            IOUtils.close(dis);
//            IOUtils.close(bis);
//        }
//        return flag;
//    }
//
//    private void sm2Verify(byte[] md, ECPoint userKey, BigInteger r,
//                           BigInteger s, Sm2Result sm2Result) {
//        sm2Result.setUpperR(null);
//        BigInteger e = new BigInteger(1, md);
//        BigInteger t = r.add(s).mod(eccN);
//        if (t.equals(BigInteger.ZERO)) {
//            return;
//        } else {
//            ECPoint x1y1 = eccPointG.multiply(sm2Result.getS());
//
//            x1y1 = x1y1.add(userKey.multiply(t));
//            sm2Result.setUpperR(e.add(x1y1.getX().toBigInteger()).mod(eccN));
//        }
//    }
//}
