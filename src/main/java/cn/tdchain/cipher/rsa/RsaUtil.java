/*
 * Copyright (c) 2017 Beijing Tiande Technology Co., Ltd.
 * All Rights Reserved.
 */
package cn.tdchain.cipher.rsa;

import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.HashMap;

import javax.crypto.Cipher;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import cn.tdchain.cipher.CipherException;
import cn.tdchain.cipher.cuda.client.CUDACipherClient;


/**
 * RSA Utility.
 *
 * @version 2.0
 * @author Xiaoming 2017-02-04
 */
public class RsaUtil{
	private static HashMap<String, PublicKey> publicKeyMap = new HashMap<String, PublicKey>();
	private static HashMap<String, PrivateKey> privateKeyMap = new HashMap<String, PrivateKey>();
	
	private static ThreadLocal<Cipher> cipherThreadLocal = new ThreadLocal<Cipher>();
	private static ThreadLocal<Signature> signatureThreadLocal = new ThreadLocal<Signature>();
    private static Provider provider = new BouncyCastleProvider();
    private static final String SIGNATURE_ALGORITHM = "SHA256withRSA";

    private RsaUtil() {
    }
    
    /**
     * cuda 如果底层有cuda支持则自动使用cuda进行解密，否则使用cpu解密。
     */
    private static boolean isCuda = false;
    private static CUDACipherClient cuda = null;
    
    static {
    	try {
			cuda = new CUDACipherClient("192.168.0.9", 8080, 4);
			if(cuda != null) {
				cn.tdchain.cipher.Cipher c = new cn.tdchain.cipher.Cipher();
				cn.tdchain.cipher.Key key = c.generateKey();
				//公钥加密
				String data = "小明老师";
				String c_text = c.encryptByPublicKey(data, key.getPublicKey());
				
				//cuda私钥解密
				String data_new = cuda.rsa_decrypt(c_text, key.getPrivateKey());
				if(data.equals(data_new)) {
					// 底层cuda能用
					isCuda = true;
				}
			}
			
			
		} catch (Exception e) {
//			e.printStackTrace();
		}
    }
    
    /**
     * @Title: generKey   
     * @Description: 生成rsa 公私钥
     * @param: @return      
     * @return: KeyPair      
     * @throws
     */
    public static KeyPair generKey() {
    	KeyPair keyPair = null;
		try {
			KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA");
			keyPairGen.initialize(1024); // 1024密钥更安全
			keyPair = keyPairGen.generateKeyPair();
		} catch (NoSuchAlgorithmException e) {
			throw new CipherException("gener key error: " + e.getMessage());
		}
        
        return keyPair;
    }

    /**
     * @Description:Encrypt by UTF-8.
     * @param data String
     * @param key cipher key
     * @return String
     */
    public static String encrypt(String data, PublicKey key) {
        if(key == null) {
        	return data;
        }
    	try {
            byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);

            byte[] enBytes = null;
            Cipher cipher = getCipher(key, Cipher.ENCRYPT_MODE);

            for (int i = 0; i < dataBytes.length; i += 116) {
                // 批次加密防止超出字节117
                enBytes = ArrayUtils.addAll(enBytes, cipher
                        .doFinal(ArrayUtils.subarray(dataBytes, i, i + 116)));
            }
            return Base64.getEncoder().encodeToString(enBytes);
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * @Title: getPublicKey   
     * @Description:   
     * @param: @param publicKey
     * @param: @return      
     * @return: String      
     * @throws
     */
    public static String getPublicKey(PublicKey publicKey) {
    	return Base64.getEncoder().encodeToString(publicKey.getEncoded());
    }
    
    /**
     * @Description: 
     * @param privateKey
     * @return
     * @throws
     */
    public static String getPrivateKey(PrivateKey privateKey) {
    	return Base64.getEncoder().encodeToString(privateKey.getEncoded());
    }
    
    
    /**
     * @Description:Get public key.
     * @param publicKeyStr String
     * @return public key
     * @throws NoSuchAlgorithmException 
     * @throws InvalidKeySpecException 
     * @throws Exception multiply exceptions
     */
    public static PublicKey getPublicKey(String publicKeyStr) throws NoSuchAlgorithmException, InvalidKeySpecException  {
    	PublicKey pubkey = publicKeyMap.get(publicKeyStr);
    	if(pubkey == null) {
    		byte[] keyBytes = Base64.getDecoder().decode(publicKeyStr);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory
                    .getInstance("RSA", provider);
            pubkey = keyFactory.generatePublic(keySpec);
            
            publicKeyMap.put(publicKeyStr, pubkey);
    	}
        
        return pubkey;
    }

    /**
     * @Description:RSA解密 2017年2月4日 xiaoming.
     * @param data String
     * @param key Key
     * @return String
     */
    public static String decrypt(String data, Key key) {
        byte[] enBytes = null;
        if (data == null || data.length() == 0) {
            return null;
        }
        if (key == null) {
            return data;
        }
        byte[] encryptedData = Base64.getDecoder().decode(data);
        try {

            Cipher cipher = getCipher(key, Cipher.DECRYPT_MODE);
            for (int i = 0; i < encryptedData.length; i += 128) {
                // 分批解密防止超出128位
                byte[] tmpData = cipher.doFinal(
                        ArrayUtils.subarray(encryptedData, i, i + 128));
                enBytes = ArrayUtils.addAll(enBytes, tmpData);
            }
            return new String(enBytes, StandardCharsets.UTF_8);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static Cipher getCipher(Key key, int mode) throws Exception {
    	Cipher cipher = cipherThreadLocal.get();
    	if(cipher == null) {
    		cipher = Cipher.getInstance("RSA/NONE/PKCS1Padding", provider);
    		cipherThreadLocal.set(cipher);
    	}
        
        cipher.init(mode, key);
        return cipher;
    }

    /**
     * @Description:签名函数,byte数组最终以16进制显示.
     * @param text 明文
     * @param privateKey private key
     * @return signed text
     */
    public static String sign(PrivateKey privateKey, String text) {
        if (privateKey == null) {
            return text;
        }
        String signText;
        try {
            Signature signature = getSignature();
            signature.initSign(privateKey);
            signature.update(text.getBytes(StandardCharsets.UTF_8));
            signText = Base64.getEncoder().encodeToString(signature.sign());
        } catch (Exception e) {
            signText = null;
        }
        return signText;
    }

    /**
     * @Description: 验签.
     * @param signText 签名后的密文
     * @param text 明文
     * @param publicKey public key
     * @return true if pass the validation
     */
    public static boolean verify(PublicKey publicKey, String signText,
                                 String text) {
        if (publicKey == null || signText == null || signText.length() == 0
                || text == null || text.length() == 0) {
            return false;
        }
        boolean signValue = false;
        try {
            Signature signature = getSignature();
            signature.initVerify(publicKey);
            signature.update(text.getBytes(StandardCharsets.UTF_8));
            signValue = signature.verify(Base64.getDecoder().decode(signText));
        } catch (Exception e) {
            signValue = false;
        }
        return signValue;
    }

    private static Signature getSignature() throws NoSuchAlgorithmException {
    	Signature signature = signatureThreadLocal.get();
    	if(signature == null) {
    		signature = Signature.getInstance(SIGNATURE_ALGORITHM);
    		signatureThreadLocal.set(signature);
    	}
       
        return signature;
    }
    
    /**
     * @Description: 公钥加密
     * @param data
     * @param publicKey
     * @return
     * @throws
     */
	public static String encrypt(String data, String publicKey) {
		PublicKey pubKey;
		try {
			pubKey = getPublicKey(publicKey);
			return encrypt(data, pubKey);
		} catch (Exception e) {
			return null;
		}
	}
	/**
	 * @Description: 公钥验签
	 * @param publickey
	 * @param signature
	 * @param data
	 * @return
	 * @throws
	 */
	public static boolean verify(String publickey, String signature, String data) {
		PublicKey pubKey;
		try {
			pubKey = getPublicKey(publickey);
			return verify(pubKey, signature, data);
		} catch (Exception e) {
			return false;
		}
	}
	
	private static PrivateKey getPrivateKey(String privateKey) throws Exception {
		PrivateKey privKey = privateKeyMap.get(privateKey);
		if(privKey == null) {
			privKey = RSAKeyStoreUtil.getPrivateKey(privateKey);
			privateKeyMap.put(privateKey, privKey);
		}
		return privKey;
	}
	/**
	 * @throws Exception 
	 * @Description: 
	 * @param data
	 * @param privateKey
	 * @return
	 * @throws
	 */
	public static String decrypt(String data, String privateKey) {
		if(isCuda) {
			//使用cuda解密
			return cuda.rsa_decrypt(data, privateKey);
		}
		
		//使用cpu解密
		try {
			return decrypt(data, getPrivateKey(privateKey));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * @throws Exception 
	 * @Description: 
	 * @param privateKeyStr
	 * @param data
	 * @return
	 * @throws
	 */
	public static String sign(String privateKeyStr, String data) {
		try {
			return sign(getPrivateKey(privateKeyStr), data);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}
