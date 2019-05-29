/*
 * Copyright (c) 2017-2018 Beijing Tiande Technology Co., Ltd.
 * All Rights Reserved.
 */
package cn.tdchain.tdmsp.util;

import java.math.BigInteger;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.Signature;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.ECPoint;
import java.security.spec.ECPublicKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

import javax.crypto.Cipher;

import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECNamedCurveParameterSpec;
import org.bouncycastle.jce.spec.ECNamedCurveSpec;

import cn.tdchain.cipher.utils.StringUtils;


/**
 * Crypto Utility.
 *
 * @version 2.0
 * @author Jiating 2018-07-18
 */
public final class EccUtil {

    private static final Charset UTF8 = StandardCharsets.UTF_8;
    private static Signature signature;

    private static ThreadLocal<Cipher> cipherThreadLocal = new ThreadLocal<Cipher>();

    static {
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    private EccUtil() {
    }

    /**
     * Encryption by key in UTF-8.
     * @param data
     * @param ecPublicKey
     * @return String
     */
    public static String encrypt(String data, ECPublicKey ecPublicKey) {
        if (StringUtils.isBlank(data) || ecPublicKey == null) {
            return data;
        }
        try {
            Cipher cipher = getCipher(ecPublicKey, Cipher.ENCRYPT_MODE);

            byte[] encryptedData = cipher.doFinal(data.getBytes(UTF8));

            return Base64.getEncoder().encodeToString(encryptedData);
        } catch (Exception e) {
//            log.error(e.getMessage(), e);
        }

        return data;
    }

    /**
     * Get public key by string.
     * 
     * @param publicKey
     * @return ECPublicKey
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeySpecException
     */
    public static ECPublicKey getPublicKey(String publicKey)
        throws NoSuchAlgorithmException, InvalidKeySpecException {
        String[] strbyte = publicKey.split("@");
        String xstr = strbyte[0];
        String ystr = strbyte[1];

        KeyFactory keyFactory = KeyFactory.getInstance(PkiConstant.EC);
        ECPoint ecPoint = new ECPoint(
                new BigInteger(1, Base64.getDecoder().decode(xstr)),
                new BigInteger(1, Base64.getDecoder().decode(ystr)));

        ECNamedCurveParameterSpec parameterSpec = ECNamedCurveTable
                .getParameterSpec(PkiConstant.SECP256K1);
        ECNamedCurveSpec spec = new ECNamedCurveSpec(PkiConstant.SECP256K1,
                parameterSpec.getCurve(), parameterSpec.getG(),
                parameterSpec.getN(), parameterSpec.getH(),
                parameterSpec.getSeed());
        ECPublicKey ecPublicKey = (ECPublicKey) keyFactory
                .generatePublic(new ECPublicKeySpec(ecPoint, spec));
        return ecPublicKey;
    }

    /**
     * Decryption by key in UTF-8.
     * 
     * @param data
     * @param privateKey
     * @return String
     */
    public static String decrypt(String data, ECPrivateKey privateKey) {
        if (privateKey == null || StringUtils.isBlank(data)) {
            return data;
        }
        try {
            byte[] encryptedData = Base64.getDecoder().decode(data);

            Cipher cipher = getCipher(privateKey, Cipher.DECRYPT_MODE);

            byte[] decryptedData = cipher.doFinal(encryptedData);
            return  Base64.getEncoder().encodeToString(decryptedData);

        } catch (Exception e) {
//            log.error(e.getMessage(), e);
        }
        return data;
    }

    /**
     * Get private key by string.
     * @param privateKey
     * @return ECPrivateKey
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeySpecException
     */
    public static ECPrivateKey getPrivateKey(String privateKey)
        throws NoSuchAlgorithmException, InvalidKeySpecException {
        KeyFactory keyFactory = KeyFactory.getInstance(PkiConstant.EC);

        PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(
        		Base64.getDecoder().decode(privateKey));
        ECPrivateKey newprivateKey = (ECPrivateKey) keyFactory
                .generatePrivate(pkcs8KeySpec);
        return newprivateKey;
    }

    /**
     * Create key pair.
     * <p>
     * Private keypairs are encoded using PKCS8 Private keys are encoded using X.509
     * </p>
     * 
     * @return ECKeyPair
     * @throws InvalidAlgorithmParameterException This is the exception for invalid or inappropriate
     *             algorithm parameters.
     * @throws NoSuchAlgorithmException This exception is thrown when a particular cryptographic
     *             algorithm is requested but is not available in the environment.
     * @throws NoSuchProviderException This exception is thrown when a particular security provider
     *             is requested but is not available in the environment.
     */
    public static KeyPair createEcKeyPair()
        throws InvalidAlgorithmParameterException, NoSuchAlgorithmException,
        NoSuchProviderException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(
                PkiConstant.ECDSA, BouncyCastleProvider.PROVIDER_NAME);
        ECGenParameterSpec ecGenParameterSpec = new ECGenParameterSpec(
                PkiConstant.SECP256K1);
        keyPairGenerator.initialize(ecGenParameterSpec,
                SecureRandomUtils.secureRandom());
        return keyPairGenerator.generateKeyPair();
    }

    /**
     * Sign data.
     * 
     * @param input source data
     * @param privateKey private key string
     * @return signature
     */
    public static String sign(String input, PrivateKey privateKey) {
        
        if (StringUtils.isBlank(input)) {
            throw new RuntimeException("msg is null");
         }
         if (null == privateKey) {
             throw new RuntimeException("privateKey is null");
         }
         
        byte[] message = input.getBytes(UTF8);
        String signMsg = "";
        try {
            Signature signature = getSignature();
            signature.initSign(privateKey);
            signature.update(message);
            byte[] res = signature.sign();
            signMsg =  Base64.getEncoder().encodeToString(res);
            
        } catch (Exception e) {
           e.printStackTrace();
        }
        
        return signMsg;
    }

   
    private static Signature getSignature() {
       if(null != signature) {
           return signature;
       }
       
       try {
           
          signature = Signature.getInstance(PkiConstant.ALGORITHM_ECC);
          
        } catch (NoSuchAlgorithmException e) {
//            log.error(e.getMessage(), e);
        }
       
       return  signature;
    }

    /**
     * Verify signature.
     * 
     * @param data source data
     * @param signature signed data
     * @param publicKey public key
     * @return true if pass the validation
     */
    public static boolean verify(String data, String signMsg,
                                 PublicKey publicKey) {
        byte[] message = data.getBytes(UTF8);
        boolean result = false;
        try {
            Signature signature = getSignature();
            signature.initVerify(publicKey);
            signature.update(message);
            
            result = signature.verify(Base64.getDecoder().decode(signMsg));
            
        } catch (Exception e) {
           e.printStackTrace();
        }
        
        return result;
    }

    private static Cipher getCipher(Key key, int mode) throws Exception {
        Cipher cipher = cipherThreadLocal.get();
        if (cipher == null) {
            cipher = Cipher.getInstance(PkiConstant.ECIES,
                    BouncyCastleProvider.PROVIDER_NAME);
            cipherThreadLocal.set(cipher);
        }
        cipher.init(mode, key);
        return cipher;
    }

}
