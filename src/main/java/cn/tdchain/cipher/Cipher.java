/*
 * Copyright (c) 2017 Beijing Tiande Technology Co., Ltd.
 * All Rights Reserved.
 */
package cn.tdchain.cipher;

import cn.tdchain.cipher.rsa.AesUtil;
import cn.tdchain.cipher.rsa.RSAKeyStoreUtil;
import cn.tdchain.cipher.rsa.RsaUtil;
import cn.tdchain.cipher.rsa.Sha256Util;
import cn.tdchain.cipher.sm.Sm2Util;
import cn.tdchain.cipher.sm.Sm3Util;
import cn.tdchain.cipher.sm.Sm4Util;

import java.security.KeyPair;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

/**
 * Description: 密钥类，封装AES、RSA、sha256、sm2、sm3、sm4算法。typer=Typer.RSA默认使用RSA体系密钥算法，typer=Typer.SM是国密体系密钥算法。
 * @author xiaoming
 * 2019年4月18日
 */
public class Cipher {
//	private  String alias = "tdbc-key";

    private Type type = Type.RSA;// 默认使用rsa体系密钥算法

    /**
     * Description: 生成Key Store 文件
     * 
     * @param path
     * @param password
     * @param alias
     */
    public void generateKeyStoreFile(String path, String password, String alias) {
        if (this.type == Cipher.Type.RSA) {
            RSAKeyStoreUtil.genKey(path, password, alias);
        } else {
//			SmKeyStoreUtil.genKeyStore(path, password, alias);
        }


    }

    /**
     * Description: 非key store方式获取公私钥
     * 
     * @return Key
     */
    public Key generateKey() {
        Key key = new Key();
        if (this.type == Cipher.Type.RSA) {
            KeyPair keyPair = RsaUtil.generKey();
            key.setPublicKey(RsaUtil.getPublicKey(keyPair.getPublic()));
            key.setPrivateKey(RsaUtil.getPrivateKey(keyPair.getPrivate()));
        } else {
            try {
                KeyPair keyPair = Sm2Util.generateSm2KeyPair();
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
     * Description: 江字符串转换成
     * 
     * @param publicKey
     * @return PublicKey
     */
    public PublicKey getPublicKey(String publicKey) {
        try {
            return RsaUtil.getPublicKey(publicKey);
        } catch (NoSuchAlgorithmException e) {
        } catch (InvalidKeySpecException e) {
        }
        return null;
    }


    /**
     * Description: 摘要算法
     * 
     * @param text
     * @return String
     */
    public String hash(String text) {
        if (this.type == Cipher.Type.RSA) {
            return Sha256Util.hash(text);
        } else {
            return Sm3Util.hash(text);
        }
    }

    /**
     * Description: 单向加密
     * 
     * @param data
     * @param passwd
     * @return String
     */
    public String encrypt(String data, String passwd) {
        if (this.type == Cipher.Type.RSA) {
            return AesUtil.encrypt(data, passwd);
        } else {
            return Sm4Util.encryptECB(data, passwd);
        }
    }

    /**
     * Description: 单向解密
     * 
     * @param data
     * @param passwd
     * @return String
     */
    public String decrypt(String data, String passwd) {
        if (this.type == Cipher.Type.RSA) {
            return AesUtil.decrypt(data, passwd);
        } else {
            return Sm4Util.decryptECB(data, passwd);
        }
    }

    /**
     * Description: 根据公钥加密
     * 
     * @param data
     * @param publicKey
     * @return String
     */
    public String encryptByPublicKey(String data, String publicKey) {
        if (this.type == Cipher.Type.RSA) {
            return RsaUtil.encrypt(data, publicKey);
        } else {
            return Sm2Util.encrypt(publicKey, data);
        }

    }

    /**
     * @param data
     * @param publicKey
     * @return String
     */
    public String encryptByPublicKey(String data, PublicKey publicKey) {
        if (this.type == Cipher.Type.RSA) {
            return RsaUtil.encrypt(data, publicKey);
        } else {
            String str = null;
            try {
                str = Sm2Util.encrypt(publicKey, data);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return str;
        }
    }


    /**
     * Description: 根据私钥解密
     * 
     * @param data
     * @param privateKey
     * @return String
     */
    public String decryptByPrivateKey(String data, PrivateKey privateKey) {
        if (this.type == Cipher.Type.RSA) {
            return RsaUtil.decrypt(data, privateKey);
        } else {
            String str = null;
            try {
                str = Sm2Util.decrypt(privateKey, data);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return str;
        }
    }

    /**
     * 
     * @param data
     * @param privateKey
     * @return String
     */
    public String decryptByPrivateKey(String data, String privateKey) {
        if (this.type == Cipher.Type.RSA) {
            return RsaUtil.decrypt(data, privateKey);
        } else {
            return Sm2Util.decrypt(privateKey, data);
        }
    }

    /**
     * Description: 私钥签名
     * 
     * @param data
     * @param privateKey
     * @return String
     */
    public String signByPrivateKey(String data, PrivateKey privateKey) {
        if (this.type == Cipher.Type.RSA) {
            return RsaUtil.sign(privateKey, data);
        } else {
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
        if (this.type == Cipher.Type.RSA) {
            return RsaUtil.sign(privateKeyStr, data);
        } else {
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
     * Description: 公钥验签
     * 
     * @param data
     * @param signature
     * @param publickey
     * @return boolean
     */
    public boolean verifyByPublicKey(String data, String signature, String publickey) {
        if (this.type == Cipher.Type.RSA) {
            return RsaUtil.verify(publickey, signature, data);
        } else {
            boolean result = false;
            try {
                result = Sm2Util.verify(publickey, signature, data);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return result;
        }
    }

    /**
     * 
     * @param data
     * @param sin_data
     * @param publicKey
     * @return boolean
     */
    public boolean verifyByPublicKey(String data, String sin_data, PublicKey publicKey) {
        if (this.type == Cipher.Type.RSA) {
            return RsaUtil.verify(publicKey, sin_data, data);
        } else {
            return Sm2Util.verify(publicKey, sin_data, data);
        }
    }


    /**
     * Description: 从公钥证书上获取公钥的字符串形式
     * 
     * @param certPath
     * @return String
     * @throws Exception
     */
    public String getPublicKeyStringByCert(String certPath) throws Exception {
        if (this.type == Cipher.Type.RSA) {
            return RSAKeyStoreUtil.getPublicKeyStr(certPath);
        } else {
            return RSAKeyStoreUtil.getPublicKeyStr(certPath);
        }
    }

    /**
     * 
     * @param certPath
     * @return PublicKey
     * @throws Exception
     */
    public PublicKey getPublicKeyByCert(String certPath) throws Exception {
        if (this.type == Cipher.Type.RSA) {
            return RSAKeyStoreUtil.getPublicKeyByCert(certPath);
        } else {
            return null;
        }
    }

    /**
     * Description: 从key store上获取公钥的字符串形式
     * 
     * @param keyStore
     * @param ksPass
     * @param alias
     * @return String
     * @throws Exception
     */
    public String getPublicKeyStringByStore(KeyStore keyStore, String ksPass, String alias) throws Exception {
        X509Certificate cert = (X509Certificate) keyStore.getCertificate(alias);
        PublicKey publicKey = cert.getPublicKey();
        String publicKeystr = Base64.getEncoder().encodeToString((publicKey.getEncoded()));
        return publicKeystr;

//        if(this.type == Cipher.Type.RSA) {
//        	return RSAKeyStoreUtil.getPublicKeyStringByKeyStore(ksPath, pubKeyAlias, ksPass);
//		}else {
////			return SmKeyStoreUtil.getPublicKeyStringByKeyStore(ksPath, ksPass);
//		    return RSAKeyStoreUtil.getPublicKeyStringByKeyStore(ksPath, pubKeyAlias, ksPass);
//		}
    }

    /**
     * 
     * @param ksPath
     * @param ksPass
     * @param alias
     * @return PublicKey
     * @throws Exception
     */
    public PublicKey getPublicKeyByStore(String ksPath, String ksPass, String alias) throws Exception {
        if (this.type == Cipher.Type.RSA) {
            return RSAKeyStoreUtil.getPublicKeyByKeyStore(ksPath, alias, ksPass);
        } else {
            return null;
        }


    }


    /**
     * Description: 从key store上获取私钥的字符串形式
     * 
     * @param keyStore
     * @param ksPasswd
     * @param alias
     * @return String
     * @throws Exception
     */
    public String getPrivateKeyStringByKeyStore(KeyStore keyStore, String ksPasswd, String alias) throws Exception {
        PrivateKey privateKey = (PrivateKey) keyStore.getKey(alias, ksPasswd.toCharArray());
        String privateKeystr = Base64.getEncoder().encodeToString((privateKey.getEncoded()));
        return privateKeystr;
//        if(this.type == Cipher.Type.RSA) {
//        	return RSAKeyStoreUtil.getPrivateKeyString(path, prikeyAlias, storePass);
//		}else {
//			return null;
//			return SmKeyStoreUtil.getPrivateKeyStringByKeyStore(path, storePass);
//		}
    }


    public PrivateKey getPrivateKeyByKeyStore(String path, String storePass, String alias) throws Exception {
        if (this.type == Cipher.Type.RSA) {
            return RSAKeyStoreUtil.getPrivateKey(path, alias, storePass);
        } else {
            return null;
        }
    }


    /**
     * Description: 设置密码算法体系 默认采用RSA
     * @param type
     */
    public void setType(Type type) {
        this.type = type;
    }


    /**
     * Description: 密钥类型 SM表示是国密/RSA表示RSA、sha256、aes
     * @author xiaoming
     * 2019年4月18日
     */
    public enum Type {
        SM, RSA
    }

    public Key newKeyInstall() {
        Key key = new Key();
        return key;
    }


//    public static void main(String[] args) throws Exception {
//        //测试数据
//        String data = "小明";
//
//
//        String ksPath = "E:\\\\keys\\\\xiaoming.ks";
//        String ksPass = "123456";
//
//        //生成key store文件
//        Cipher cipher = new Cipher();
//        cipher.setType(Cipher.Type.RSA);
////		cipher.generateKeyStoreFile(ksPath, ksPass);
////		cipher.getKeyStore(ksPath, ksPass);
//
//
////		Key key = cipher.generateKey();
////		String privateKey = key.getPrivateKey();
////		String publicKey = key.getPublicKey(); 
//
//
//        String hash = cipher.hash(data);
//        if (SoutUtil.isOpenSout())
//            System.out.println(hash.length());
//
//        //获取公私钥
////		String publicKey = cipher.getPublicKeyStringByStore(ksPath, ksPass);
////		String privateKey = cipher.getPrivateKeyStringByKeyStore(ksPath, ksPass);
////		String publicKey = cipher.getPublicKeyStringByCert("");
//
//        //非对称加密解密测试
////		String data_ci = cipher.encryptByPublicKey(data, publicKey);
////		System.out.println(data_ci);
////		
//////		String new_data = cipher.decryptByPrivateKey(data_ci, privateKey);
////		System.out.println(new_data);
////		
////		//签名验签测试
////		String sin_data = cipher.signByPrivateKey(data, privateKey);
////		boolean flag = cipher.verifyByPublicKey(data, sin_data, publicKey);
////		System.out.println(flag);
////		
////		
////		//单向加密测试
////		String data_aes_ci = cipher.encrypt(data, ksPass);
////		String new_aes_data = cipher.decrypt(data_aes_ci, ksPass);
////		System.out.println(new_aes_data);
//    }

}
