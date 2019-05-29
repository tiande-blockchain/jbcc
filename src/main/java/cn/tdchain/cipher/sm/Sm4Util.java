package cn.tdchain.cipher.sm;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import cn.tdchain.cipher.utils.CipherUtil;
import cn.tdchain.cipher.utils.StringUtils;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.Security;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class Sm4Util {
    public static final String ALGORITHM_NAME = "SM4";
    public static final String ALGORITHM_NAME_ECB_PADDING = "SM4/ECB/PKCS5Padding";
//    public static final String ALGORITHM_NAME_ECB_PADDING = "SM4/ECB/PKCS7Padding";
    public static final String ALGORITHM_NAME_CBC_PADDING = "SM4/CBC/PKCS5Padding";
    public static final int DEFAULT_KEY_SIZE = 128;
    
    static {
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    public static byte[] generateKey() throws NoSuchAlgorithmException, NoSuchProviderException {
        return generateKey(DEFAULT_KEY_SIZE);
    }

    public static byte[] generateKey(int keySize) throws NoSuchAlgorithmException, NoSuchProviderException {
        KeyGenerator kg = KeyGenerator.getInstance(ALGORITHM_NAME, BouncyCastleProvider.PROVIDER_NAME);
        kg.init(keySize, new SecureRandom());
        return kg.generateKey().getEncoded();
    }

    public static byte[] encrypt_Ecb_Padding(byte[] key, byte[] data)
        throws InvalidKeyException, NoSuchAlgorithmException, NoSuchProviderException,
        NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
        Cipher cipher = generateEcbCipher(ALGORITHM_NAME_ECB_PADDING, Cipher.ENCRYPT_MODE, key);
        return cipher.doFinal(data);
    }

    public static byte[] decrypt_Ecb_Padding(byte[] key, byte[] cipherText)
        throws IllegalBlockSizeException, BadPaddingException, InvalidKeyException,
        NoSuchAlgorithmException, NoSuchProviderException, NoSuchPaddingException {
        Cipher cipher = generateEcbCipher(ALGORITHM_NAME_ECB_PADDING, Cipher.DECRYPT_MODE, key);
        return cipher.doFinal(cipherText);
    }

    public static byte[] encrypt_Cbc_Padding(byte[] key, byte[] iv, byte[] data)
        throws InvalidKeyException, NoSuchAlgorithmException, NoSuchProviderException,
        NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException,
        InvalidAlgorithmParameterException {
        Cipher cipher = generateCbcCipher(ALGORITHM_NAME_CBC_PADDING, Cipher.ENCRYPT_MODE, key, iv);
        return cipher.doFinal(data);
    }

    public static byte[] decrypt_Cbc_Padding(byte[] key, byte[] iv, byte[] cipherText)
        throws IllegalBlockSizeException, BadPaddingException, InvalidKeyException,
        NoSuchAlgorithmException, NoSuchProviderException, NoSuchPaddingException,
        InvalidAlgorithmParameterException {
        Cipher cipher = generateCbcCipher(ALGORITHM_NAME_CBC_PADDING, Cipher.DECRYPT_MODE, key, iv);
        return cipher.doFinal(cipherText);
    }

    private static Cipher generateEcbCipher(String algorithmName, int mode, byte[] key)
        throws NoSuchAlgorithmException, NoSuchProviderException, NoSuchPaddingException,
        InvalidKeyException {
        Cipher cipher = Cipher.getInstance(algorithmName, BouncyCastleProvider.PROVIDER_NAME);
        Key sm4Key = new SecretKeySpec(key, ALGORITHM_NAME);
        cipher.init(mode, sm4Key);
        return cipher;
    }

    private static Cipher generateCbcCipher(String algorithmName, int mode, byte[] key, byte[] iv)
        throws InvalidKeyException, InvalidAlgorithmParameterException, NoSuchAlgorithmException,
        NoSuchProviderException, NoSuchPaddingException {
        Cipher cipher = Cipher.getInstance(algorithmName, BouncyCastleProvider.PROVIDER_NAME);
        Key sm4Key = new SecretKeySpec(key, ALGORITHM_NAME);
        IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);
        cipher.init(mode, sm4Key, ivParameterSpec);
        return cipher;
    }

    /**
     * @param data
     * @param passwd
     * @return String
     */
    public static String encryptECB(String data, String passwd) {
        String encryptStr = null;
        passwd = CipherUtil.zeroSuffix(passwd);
        
        try {
            
            byte[] cryptByte =  encrypt_Ecb_Padding(StringUtils.getBytes(passwd),StringUtils.getBytes(data));
            encryptStr = Base64.getEncoder().encodeToString(cryptByte);
        } catch (Exception e) {
           e.printStackTrace();
        }
        
        return encryptStr;
    }

    /**
     * @param data
     * @param passwd
     * @return String
     */
    public static String decryptECB(String data, String passwd) {
        String decryptStr = null;
        passwd = CipherUtil.zeroSuffix(passwd);
        
        try {
            
            byte[] cryptByte =  decrypt_Ecb_Padding(StringUtils.getBytes(passwd),Base64.getDecoder().decode(data));
//            decryptStr = Base64Utils.encodeToString(cryptByte);
            decryptStr =  StringUtils.newStr(cryptByte);
        } catch (Exception e) {
           e.printStackTrace();
        }
        
        return decryptStr;
    }
}
