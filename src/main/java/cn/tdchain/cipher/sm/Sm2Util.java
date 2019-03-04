/*
 * Copyright (c) 2017-2018 Beijing Tiande Technology Co., Ltd.
 * All Rights Reserved.
 */
package cn.tdchain.cipher.sm;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import org.bouncycastle.crypto.engines.SM2Engine;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.crypto.params.ParametersWithRandom;
import org.bouncycastle.crypto.signers.SM2Signer;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPrivateKey;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPublicKey;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECNamedCurveGenParameterSpec;
import org.bouncycastle.jce.spec.ECParameterSpec;

import cn.tdchain.cipher.utils.StringUtils;


/**
 * SM Utility.
 *
 * @version 2.0
 * @author Lijiating 2018-10-12
 */
public final class Sm2Util {

    private static final Charset UTF8 = StandardCharsets.UTF_8;
    private static final String EC = "EC";
    private static final String SM2P256V1 = "sm2p256v1";
    static {
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    private Sm2Util() {
    }

    /**
     * Generate SM2 keys.
     * 
     * @return KeyPair
     * @throws Exception NoSuchAlgorithmException, NoSuchProviderException,
     *             InvalidAlgorithmParameterException
     */
    public static KeyPair generateSm2KeyPair() throws Exception {
        KeyPairGenerator g = KeyPairGenerator.getInstance(EC,
                BouncyCastleProvider.PROVIDER_NAME);
        g.initialize(new ECNamedCurveGenParameterSpec(SM2P256V1),
                new SecureRandom());
        return g.generateKeyPair();
    }

    /**
     * SM2私钥字符串转换为对象.
     * 
     * @param privateKeyStr private key string
     * @return PrivateKey
     * @throws Exception InvalidKeySpecException, NoSuchAlgorithmException, NoSuchProviderException
     */
    public static PrivateKey getPrivateKey(String privateKeyStr)
        throws Exception {
        if (StringUtils.isBlank(privateKeyStr)) {
            throw new NullPointerException();
        }
        byte[] keyBytes = Base64.getDecoder().decode(privateKeyStr);
        PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(EC,
                BouncyCastleProvider.PROVIDER_NAME);
        return keyFactory.generatePrivate(pkcs8KeySpec);
    }

    /**
     * SM2公钥字符串转换为对象.
     * 
     * @param publicKeyStr public key string
     * @return PublicKey
     * @throws Exception InvalidKeySpecException, NoSuchAlgorithmException, NoSuchProviderException
     */
    public static PublicKey getPublicKey(String publicKeyStr) throws Exception {
        if (StringUtils.isBlank(publicKeyStr)) {
            throw new NullPointerException();
        }
        byte[] keyBytes = Base64.getDecoder().decode(publicKeyStr);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(EC,
                BouncyCastleProvider.PROVIDER_NAME);
        return keyFactory.generatePublic(keySpec);
    }

    /**
     * SM2公钥加密.
     *
     * @param publicKey public key
     * @param src source data
     * @return SM2密文，实际包含三部分：ECC公钥、真正的密文、公钥和原文的SM3-HASH值
     * @throws Exception encrypt exception
     */
    public static String encrypt(PublicKey publicKey, String src)
        throws Exception {
        if (StringUtils.isBlank(src) || publicKey == null) {
            throw new NullPointerException();
        }
        byte[] srcData = src.getBytes(UTF8);
        if (publicKey instanceof BCECPublicKey) {
            BCECPublicKey localECPublicKey = (BCECPublicKey) publicKey;
            ECParameterSpec localECParameterSpec = localECPublicKey
                    .getParameters();
            ECDomainParameters localECDomainParameters = new ECDomainParameters(
                    localECParameterSpec.getCurve(),
                    localECParameterSpec.getG(), localECParameterSpec.getN());
            ECPublicKeyParameters publiceyParam = new ECPublicKeyParameters(
                    localECPublicKey.getQ(), localECDomainParameters);
            SM2Engine engine = new SM2Engine();
            ParametersWithRandom pwr = new ParametersWithRandom(publiceyParam,
                    new SecureRandom());
            engine.init(true, pwr);
            byte[] data = engine.processBlock(srcData, 0, srcData.length);
            return Base64.getEncoder().encodeToString(data);
        } else {
            return src;
        }
    }

    /**
     * SM2私钥解密.
     *
     * @param privateKey private key
     * @param src SM2密文，实际包含三部分：ECC公钥、真正的密文、公钥和原文的SM3-HASH值
     * @return 原文
     * @throws Exception decrypt exception
     */
    public static String decrypt(PrivateKey privateKey, String src)
        throws Exception {
        if (StringUtils.isBlank(src) || privateKey == null) {
            throw new NullPointerException();
        }
        byte[] srcData = Base64.getDecoder().decode(src);
        if (privateKey instanceof BCECPrivateKey) {
            BCECPrivateKey localECPrivateKey = (BCECPrivateKey) privateKey;
            ECParameterSpec localECParameterSpec = localECPrivateKey
                    .getParameters();
            ECDomainParameters localECDomainParameters = new ECDomainParameters(
                    localECParameterSpec.getCurve(),
                    localECParameterSpec.getG(), localECParameterSpec.getN());
            ECPrivateKeyParameters prikeyParam = new ECPrivateKeyParameters(
                    localECPrivateKey.getD(), localECDomainParameters);
            SM2Engine engine = new SM2Engine();
            engine.init(false, prikeyParam);
            byte[] data = engine.processBlock(srcData, 0, srcData.length);
            return Base64.getEncoder().encodeToString(data);
        } else {
            return src;
        }
    }

    /**
     * SM2私钥签名.
     *
     * @param privateKey private key
     * @param src source data String
     * @return signature
     * @throws Exception signature exception
     */
    public static String sign(PrivateKey privateKey, String src)
        throws Exception {
        if (StringUtils.isBlank(src) || privateKey == null) {
            throw new NullPointerException();
        }
        byte[] srcData = src.getBytes(UTF8);
        if (privateKey instanceof BCECPrivateKey) {
            BCECPrivateKey localECPrivateKey = (BCECPrivateKey) privateKey;
            ECParameterSpec localECParameterSpec = localECPrivateKey
                    .getParameters();
            ECDomainParameters localECDomainParameters = new ECDomainParameters(
                    localECParameterSpec.getCurve(),
                    localECParameterSpec.getG(), localECParameterSpec.getN());
            ECPrivateKeyParameters prikeyParam = new ECPrivateKeyParameters(
                    localECPrivateKey.getD(), localECDomainParameters);
            SM2Signer signer = new SM2Signer();
            ParametersWithRandom pwr = new ParametersWithRandom(prikeyParam,
                    new SecureRandom());
            signer.init(true, pwr);
            signer.update(srcData, 0, srcData.length);
            byte[] data = signer.generateSignature();
            return Base64.getEncoder().encodeToString(data);
        } else {
            return src;
        }
    }

    /**
     * SM2公钥验签.
     *
     * @param publicKey public key
     * @param src source data String
     * @param sign signature
     * @return verify result: true or false
     */
    public static boolean verify(PublicKey publicKey, String sign, String src) {
        if (StringUtils.isBlank(src) || StringUtils.isBlank(sign)
                || publicKey == null) {
            throw new NullPointerException();
        }

        if (publicKey instanceof BCECPublicKey) {
            BCECPublicKey localECPublicKey = (BCECPublicKey) publicKey;
            ECParameterSpec localECParameterSpec = localECPublicKey
                    .getParameters();
            ECDomainParameters localECDomainParameters = new ECDomainParameters(
                    localECParameterSpec.getCurve(),
                    localECParameterSpec.getG(), localECParameterSpec.getN());
            ECPublicKeyParameters publiceyParam = new ECPublicKeyParameters(
                    localECPublicKey.getQ(), localECDomainParameters);
            SM2Signer signer = new SM2Signer();
            signer.init(false, publiceyParam);
            byte[] srcData = src.getBytes(UTF8);
            signer.update(srcData, 0, srcData.length);
            return signer.verifySignature(Base64.getDecoder().decode(sign));
        } else {
            return false;
        }
    }

    /**
     * @param publicKey
     * @param data
     * @return
     */
    public static String encrypt(String publicKey, String data) {
        String str = null;
        
        try {
            PublicKey pKey = getPublicKey(publicKey);
            str =  encrypt(pKey,data);
        } catch (Exception e) {
           e.printStackTrace();
        }
      
        return str;
        
    }

    /**
     * @param privateKey
     * @param data
     * @return
     */
    public static String decrypt(String privateKey, String data) {
        String str = null;
        
        try {
            PrivateKey pKey = getPrivateKey(privateKey);
            str =  decrypt(pKey,data);
            str =StringUtils.newStr(Base64.getDecoder().decode(str));
        } catch (Exception e) {
           e.printStackTrace();
        }
      
        return str;
    }

    /**
     * @param privateKeyStr
     * @param data
     * @return
     */
    public static String sign(String privateKeyStr, String data) {
        String str = null;
        
        try {
            PrivateKey pKey = getPrivateKey(privateKeyStr);
            str =  sign(pKey,data);
        } catch (Exception e) {
           e.printStackTrace();
        }
      
        return str;
    }

    /**
     * @param publickey
     * @param signature
     * @param data
     * @return
     */
    public static boolean verify(String publickey, String signature,
                                String data) {
        boolean result = false;
        
        try {
            PublicKey pKey = getPublicKey(publickey);
            result =  verify(pKey, signature, data);
        } catch (Exception e) {
           e.printStackTrace();
        }
      
        return result;
    }

}
