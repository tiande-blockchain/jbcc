/*
 * Copyright (c) 2017 Beijing Tiande Technology Co., Ltd.
 * All Rights Reserved.
 */
package cn.tdchain.cipher.rsa;

import java.nio.charset.StandardCharsets;
import java.security.Provider;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import cn.tdchain.cipher.utils.CipherUtil;

/**
 * AES Utility.
 * @version 2.0
 * @author Xiaoming
 */
public class AesUtil {
	private static ThreadLocal<Cipher> cipherThreadLocal = new ThreadLocal<Cipher>();
    private static Provider provider = new BouncyCastleProvider();
    private static final String VIPARA = "aabbccddeeffgghh"; // AES 16bytes. DES 18bytes

    private AesUtil() {
    }

    /**
     * Encrpt by UTF-8.
     * 
     * @param data String
     * @param passwd String
     * @return String
     */
    public static String encrypt(String data, String passwd) {
        try {
            Cipher cipher = getCipher(passwd, Cipher.ENCRYPT_MODE);
            
            return Base64.getEncoder().encodeToString(
                    cipher.doFinal(data.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
        	 System.out.println("Failed to encrypt data. " + e.getMessage());
            return data;
        }
    }

    /**
     * Decrypt by UTF-8 and BASE64.
     * 
     * @param data String
     * @param passwd String
     * @return String
     */
    public static String decrypt(String data, String passwd) {
        if (data == null || data.length() == 0) {
            return data;
        }
        try {
        	
            byte[] encryptedData = Base64.getDecoder().decode(data);

            Cipher cipher = getCipher(passwd, Cipher.DECRYPT_MODE);
            byte[] decryptedData = cipher.doFinal(encryptedData);

            return new String(decryptedData, StandardCharsets.UTF_8);
        } catch (Exception e) {
            System.out.println("Failed to aes decrypt data." + e.getMessage());
            return null;
        }
    }

	private static Cipher getCipher(String passwd, int mode) throws Exception {
		String formattedPwd = CipherUtil.zeroSuffix(passwd);
		SecretKeySpec key = new SecretKeySpec(formattedPwd.getBytes(StandardCharsets.UTF_8), "AES");
		IvParameterSpec zeroIv = new IvParameterSpec(VIPARA.getBytes(StandardCharsets.UTF_8));
		Cipher cipher = cipherThreadLocal.get();
        if (cipher == null) {
            cipher = Cipher.getInstance("AES", provider);
            cipherThreadLocal.set(cipher);
        }
        cipher.init(mode, key, zeroIv);
        return cipher;
	}

}
