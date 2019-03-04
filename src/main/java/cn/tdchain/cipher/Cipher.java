package cn.tdchain.cipher;

import java.security.KeyPair;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;

import cn.tdchain.cipher.rsa.AesUtil;
import cn.tdchain.cipher.rsa.RSAKeyStoreUtil;
import cn.tdchain.cipher.rsa.RsaUtil;
import cn.tdchain.cipher.rsa.Sha256Util;
import cn.tdchain.cipher.sm.Sm3Util;
import cn.tdchain.cipher.sm.Sm4Util;
import cn.tdchain.cipher.sm.Sm2Util;
import cn.tdchain.tdmsp.util.PkiConstant;
import cn.tdchain.tdmsp.util.PkiUtil;

/**
 * @Description: 密钥类，封装AES、RSA、sha256、sm2、sm3、sm4算法。typer=Typer.RSA默认使用RSA体系密钥算法，typer=Typer.SM是国密体系密钥算法。
 * @author xiaoming
 * @date:上午10:36:26
 */
public class Cipher {
//	private  String alias = "tdbc-key";
	
	private  Type type = Type.RSA;// 默认使用rsa体系密钥算法
	
	/**
	 * @Description: 生成Key Store 文件
	 * @param path KeyStore 文件绝对路径
	 * @param password
	 * @param alias
	 * @throws
	 */
	public void generateKeyStoreFile(String path, String password, String alias) {
		if(this.type == Cipher.Type.RSA) {
			RSAKeyStoreUtil.genKey(path, password, alias);
		}else {
//			SmKeyStoreUtil.genKeyStore(path, password, alias);
		}
		
		
		
	} 
	
	/**
	 * @Description: 获取keystore对象
	 * @param path
	 * @param password
	 * @return
	 * @throws
	 */
	public KeyStore getKeyStore(String path, String password) {
		KeyStore keyStore = null;
	    try {
	        
	        keyStore = PkiUtil.getKeyStore(path, password, PkiConstant.PKCS12);
	        
        } catch (Exception e) {
          e.printStackTrace();
        }
	    
	    return keyStore;
	}
	
	
	/**
	 * @Description: 非key store方式获取公私钥
	 * @return
	 * @throws
	 */
	public Key generateKey() {
		Key key = new Key();
		if(this.type == Cipher.Type.RSA) {
			KeyPair keyPair = RsaUtil.generKey();
			key.setPublicKey(RsaUtil.getPublicKey(keyPair.getPublic()));
			key.setPrivateKey(RsaUtil.getPrivateKey(keyPair.getPrivate()));
		}else {
		    try {
    		    KeyPair keyPair =  Sm2Util.generateSm2KeyPair();
    			String privateKey = Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded());
    			String publicKey = Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());
    			
    			key.setPrivateKey(privateKey);
    			key.setPublicKey(publicKey);
    			
		    } catch (Exception e) {
		        e.printStackTrace();
		    }
		}
		return key;
	}
	
	
	/**
	 * @Description: 摘要算法
	 * @param text
	 * @return
	 * @throws
	 */
	public String hash(String text) {
		if(this.type == Cipher.Type.RSA) {
			return Sha256Util.hash(text);
		}else {
			return Sm3Util.hash(text);
		}
	}
	
	/**
	 * @Description: 单向加密
	 * @param data
	 * @param passwd
	 * @return
	 * @throws
	 */
	public String encrypt(String data, String passwd) {
        if(this.type == Cipher.Type.RSA) {
        	return AesUtil.encrypt(data, passwd);
		}else {
			return Sm4Util.encryptECB(data, passwd);
		}
	}
	
	/**
	 * @Description: 单向解密
	 * @param data
	 * @param passwd
	 * @return 如果解密失败返回null
	 * @throws
	 */
	public String decrypt(String data, String passwd) {
        if(this.type == Cipher.Type.RSA) {
        	return AesUtil.decrypt(data, passwd);
		}else {
			return Sm4Util.decryptECB(data, passwd);
		}
	}
	
	/**
	 * @Description: 根据公钥加密
	 * @param data
	 * @param publicKey
	 * @return
	 * @throws
	 */
    public String encryptByPublicKey(String data, String publicKey) {
        if(this.type == Cipher.Type.RSA) {
        	return RsaUtil.encrypt(data, publicKey);
		}else {
			return Sm2Util.encrypt(publicKey, data);
		}
		
	}
    
    /**
     * @Description: 
     * @param data
     * @param publicKey
     * @return
     * @throws
     */
    public String encryptByPublicKey(String data, PublicKey publicKey) {
    	if(this.type == Cipher.Type.RSA) {
        	return RsaUtil.encrypt(data, publicKey);
		}else {
		    String str = null;
		    try {
		        str =  Sm2Util.encrypt(publicKey,data);
            } catch (Exception e) {
                e.printStackTrace();
            }
			return str;
		}
	}
    
    
    /**
     * @Description: 根据私钥解密
     * @param data 密文
     * @param privateKey
     * @return 如果解密失败返回null
     * @throws
     */
    public String decryptByPrivateKey(String data, PrivateKey privateKey) {
        if(this.type == Cipher.Type.RSA) {
        	return RsaUtil.decrypt(data, privateKey);
		}else {
		    String str = null;
            try {
                str =  Sm2Util.decrypt(privateKey, data);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return str;
		}
	}
    
    /**
     * @throws Exception 
     * @Description: 
     * @param data
     * @param privateKey
     * @return
     * @throws
     */
    public String decryptByPrivateKey(String data, String privateKey) {
        if(this.type == Cipher.Type.RSA) {
        	return RsaUtil.decrypt(data, privateKey);
		}else {
			return Sm2Util.decrypt(privateKey, data);
		}
	}
    
    /**
     * @Description: 私钥签名
     * @param data
     * @param privateKey
     * @return
     * @throws
     */
    public String signByPrivateKey(String data, PrivateKey privateKey) {
        if(this.type == Cipher.Type.RSA) {
        	return RsaUtil.sign(privateKey, data);
		}else {
		    String str = null;
		    try {
		        str = Sm2Util.sign(privateKey, data);
		        
            } catch (Exception e) {
                e.printStackTrace();
            }
		    
			return str;
		}
    }
    
    public String signByPrivateKey(String data, String privateKeyStr) {
    	if(this.type == Cipher.Type.RSA) {
        	return RsaUtil.sign(privateKeyStr, data);
		}else {
		    String str = null;
            try {
                str = Sm2Util.sign(privateKeyStr, data);
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            return str;
		}
	}

    
    /**
     * @Description: 公钥验签
     * @param data 签名前明文
     * @param signature 私钥签名后密文
     * @param publickey 
     * @return 
     * @throws
     */
    public boolean verifyByPublicKey(String data, String signature, String publickey) {
        if(this.type == Cipher.Type.RSA) {
        	return RsaUtil.verify(publickey, signature, data);
		}else {
		    boolean result = false;
            try {
                result =  Sm2Util.verify(publickey, signature, data);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return result;
		}
    }
    
    /**
     * @Description: 
     * @param data
     * @param sin_data
     * @param publicKey
     * @return
     * @throws
     */
    public boolean verifyByPublicKey(String data, String sin_data, PublicKey publicKey) {
    	if(this.type == Cipher.Type.RSA) {
        	return RsaUtil.verify(publicKey, sin_data, data);
		}else {
		    return Sm2Util.verify(publicKey, sin_data, data);
		}
	}
    
    
    /**
     * @throws Exception 
     * @Description: 从公钥证书上获取公钥的字符串形式
     * @param certPath
     * @return
     * @throws
     */
    public String getPublicKeyStringByCert(String certPath) throws Exception {
        if(this.type == Cipher.Type.RSA) {
        	return RSAKeyStoreUtil.getPublicKeyStr(certPath);
		}else {
		    return RSAKeyStoreUtil.getPublicKeyStr(certPath);
		}
    }
    
    /**
     * @Description: 
     * @param certPath
     * @return
     * @throws Exception
     * @throws
     */
    public PublicKey getPublicKeyByCert(String certPath) throws Exception {
        if(this.type == Cipher.Type.RSA) {
        	return RSAKeyStoreUtil.getPublicKeyByCert(certPath);
		}else {
			return null;
		}
    }
    
    /**
     * @throws Exception 
     * @Description: 从key store上获取公钥的字符串形式
     * @param certPath
     * @return
     * @throws
     */
    public String getPublicKeyStringByStore(String ksPath, String ksPass, String pubKeyAlias) throws Exception {
        if(this.type == Cipher.Type.RSA) {
        	return RSAKeyStoreUtil.getPublicKeyStringByKeyStore(ksPath, pubKeyAlias, ksPass);
		}else {
//			return SmKeyStoreUtil.getPublicKeyStringByKeyStore(ksPath, ksPass);
		    return RSAKeyStoreUtil.getPublicKeyStringByKeyStore(ksPath, pubKeyAlias, ksPass);
		}
    }
    
    /**
     * @Description: 
     * @param ksPath
     * @param alias
     * @param ksPass
     * @return
     * @throws Exception
     * @throws
     */
    public PublicKey getPublicKeyByStore(String ksPath, String ksPass, String alias) throws Exception {
        if(this.type == Cipher.Type.RSA) {
        	return RSAKeyStoreUtil.getPublicKeyByKeyStore(ksPath, alias, ksPass);
		}else {
			return null;
		}
        
        
    }
    
    
    /**
     * @throws Exception 
     * @Description: 从key store上获取私钥的字符串形式
     * @param path
     * @param alias
     * @param storePass
     * @return
     * @throws
    */ 
    public String getPrivateKeyStringByKeyStore(String path, String storePass, String prikeyAlias) throws Exception {
//    	KeyStore keyStore =  getKeyStore(path, storePass);
//        PrivateKey privateKey = (PrivateKey) keyStore.getKey(prikeyAlias, prikeyAlias.toCharArray());
//        String privateKeystr = Base64.getEncoder().encodeToString((privateKey.getEncoded()));
//        return privateKeystr;
        if(this.type == Cipher.Type.RSA) {
        	return RSAKeyStoreUtil.getPrivateKeyString(path, prikeyAlias, storePass);
		}else {
			return null;
//			return SmKeyStoreUtil.getPrivateKeyStringByKeyStore(path, storePass);
		}
    }
    
    
    public PrivateKey getPrivateKeyByKeyStore(String path, String storePass, String alias) throws Exception {
        if(this.type == Cipher.Type.RSA) {
        	return RSAKeyStoreUtil.getPrivateKey(path, alias, storePass);
		}else {
			return null;
		}
    }
    
	
	/**
	 * @Description: 设置密码算法体系 默认采用RSA
	 * @param typer
	 * @throws
	 */
	public void setType(Type type) {
		this.type = type;
	}


	/**
	 * @Description: 密钥类型 SM表示是国密/RSA表示RSA、sha256、aes
	 * @author xiaoming
	 * @date:上午10:42:32
	 */
	public enum Type{
		SM,RSA
	}
	
	public Key newKeyInstall() {
		Key key = new Key();
		return key;
	}
	
	
	public static void main(String[] args) throws Exception {
		//测试数据
		String data = "小明";
				
				
		String ksPath = "E:\\\\keys\\\\xiaoming.ks";
		String ksPass = "123456";
		
		//生成key store文件
		Cipher cipher = new Cipher();
		cipher.setType(Cipher.Type.SM);
//		cipher.generateKeyStoreFile(ksPath, ksPass);
//		cipher.getKeyStore(ksPath, ksPass);
		
		
//		Key key = cipher.generateKey();
//		String privateKey = key.getPrivateKey();
//		String publicKey = key.getPublicKey(); 
		
		
		String hash = cipher.hash(data);
		System.out.println(hash.length());
		
		//获取公私钥
//		String publicKey = cipher.getPublicKeyStringByStore(ksPath, ksPass);
//		String privateKey = cipher.getPrivateKeyStringByKeyStore(ksPath, ksPass);
//		String publicKey = cipher.getPublicKeyStringByCert("");
		
		//非对称加密解密测试
//		String data_ci = cipher.encryptByPublicKey(data, publicKey);
//		System.out.println(data_ci);
//		
////		String new_data = cipher.decryptByPrivateKey(data_ci, privateKey);
//		System.out.println(new_data);
//		
//		//签名验签测试
//		String sin_data = cipher.signByPrivateKey(data, privateKey);
//		boolean flag = cipher.verifyByPublicKey(data, sin_data, publicKey);
//		System.out.println(flag);
//		
//		
//		//单向加密测试
//		String data_aes_ci = cipher.encrypt(data, ksPass);
//		String new_aes_data = cipher.decrypt(data_aes_ci, ksPass);
//		System.out.println(new_aes_data);
	}

}
