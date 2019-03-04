///*
// * Copyright (c) 2017 Beijing Tiande Technology Co., Ltd.
// * All Rights Reserved.
// */
//package cn.tdchain.cipher.sm.copy;
//
//import java.util.Base64;
//import java.util.Date;
//
//import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
//import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
//import org.bouncycastle.crypto.params.ECPublicKeyParameters;
//
//import cn.tdchain.cipher.utils.ByteUtils;
//import cn.tdchain.cipher.utils.CipherUtil;
//import cn.tdchain.cipher.utils.FileUtils;
//
//
///**
// * SM Key Store Utility.
// *
// * @version 2.0
// * @author Houmj 2017-10-12
// */
//public class SmKeyStoreUtil {
//
//    private SmKeyStoreUtil() {
//    }
//    
//    /**
//     * @Description: 创建一个sm2的keystore文件
//     * @param path
//     * @param password
//     * @param alias
//     * @throws
//     */
//    public static synchronized void genKeyStore(String path, String password, String alias) {
//        AsymmetricCipherKeyPair keypair = Sm2Util.getInstance()
//                .generateKeyPair();
//        SmKeyStore smKeyStore = new SmKeyStore();
//        Date date = new Date();
//        smKeyStore.setCreateTime(date);
//        smKeyStore.setUpdateTime(date);
//        smKeyStore.setAuthor("tdbc-sm");
//        smKeyStore.setPrivateKey(SmKeyStoreUtil.genPrivateKey(password, keypair));
//        smKeyStore.setPublicKey(SmKeyStoreUtil.genPublicKey(password, keypair));
//        smKeyStore.setCert(SmKeyStoreUtil.genCert(keypair, path + ".cert"));
//        
//        String text = Base64.getEncoder().encodeToString(smKeyStore.getPrivateKey())
//                + smKeyStore.getCreateTime().getTime()
//                + smKeyStore.getUpdateTime().getTime() + smKeyStore.getAuthor();
//        
//        smKeyStore.setSummary(Sm3Digest.hash(text));
//        
//        
//        try {
//            String signature = Sm2Util.getInstance().sign("user",ByteUtils.get32Bytes(((ECPrivateKeyParameters) keypair.getPrivate()).getD()),smKeyStore.getSummary());
//            smKeyStore.setSignature(signature);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        
//       
//        FileUtils.saveFile(path, smKeyStore);
//    }
//    
//
//    private static byte[] genPublicKey(String password, AsymmetricCipherKeyPair keypair) {
//    	 try {
//             byte[] dBytes = ((ECPublicKeyParameters) keypair.getPublic()).getQ().getEncoded();
//             String encode = Base64.getEncoder().encodeToString(dBytes);
//             String encryptEcbData = Sm4Util.getInstance().encryptECB(encode, password);
//             return Base64.getDecoder().decode(encryptEcbData);
//         } catch (Exception e) {
//             e.printStackTrace();
//             return null;
//         }
//	}
//
//	/**
//     * Get private key by key pair.
//     * 
//     * @param password String
//     * @param kp AsymmetricCipherKeyPair
//     * @return private key
//     */
//    public static byte[] genPrivateKey(String password,
//                                       AsymmetricCipherKeyPair kp) {
//        try {
//            byte[] dBytes = ByteUtils.get32Bytes(
//                    ((ECPrivateKeyParameters) kp.getPrivate()).getD());
//            String encode;
//            encode = Base64.getEncoder().encodeToString(dBytes);
//            String encryptEcbData = Sm4Util.getInstance().encryptECB(encode,
//                    password);
//            return Base64.getDecoder().decode(encryptEcbData);
//        } catch (Exception e) {
//            e.printStackTrace();
//            return null;
//        }
//    }
//
//    /**
//     * Generate certification.
//     * 
//     * @param kp AsymmetricCipherKeyPair
//     * @param path String
//     * @return SmCert
//     */
//    public static SmCert genCert(AsymmetricCipherKeyPair kp, String path) {
//        try {
//            return new SmCert(
//                    ((ECPublicKeyParameters) kp.getPublic()).getQ()
//                            .getEncoded(),
//                    ByteUtils.get32Bytes(
//                            ((ECPrivateKeyParameters) kp.getPrivate()).getD()),
//                    path);
//        } catch (Exception e) {
//            e.printStackTrace();
//            return null;
//        }
//    }
//
//    /**
//     * Get private key from key store.
//     * 
//     * @param path String
//     * @param password String
//     * @return private key bytes
//     */
//    public static String getPrivateKeyStringByKeyStore(String path, String password) {
//        SmKeyStore smKeyStore = (SmKeyStore) FileUtils.readFile(path);
//        try {
//            String encode = Base64.getEncoder().encodeToString(smKeyStore.getPrivateKey());
//            return Sm4Util.getInstance().decryptECB(encode, password);
//        } catch (Exception e) {
//            e.printStackTrace();
//            return null;
//        }
//    }
//    
//    /**
//     * @Description: 
//     * @param path
//     * @param password
//     * @return
//     * @throws
//     */
//    public static String getPublicKeyStringByKeyStore(String path, String password) {
//    	SmKeyStore smKeyStore = (SmKeyStore) FileUtils.readFile(path);
//        try {
//        	String encode = Base64.getEncoder().encodeToString(smKeyStore.getPublicKey());
//        	return Sm4Util.getInstance().decryptECB(encode, password);
//        } catch (Exception e) {
//            e.printStackTrace();
//            return null;
//        }
//    }
//
//    /**
//     * Get public key from key store.
//     * 
//     * @param path String
//     * @return public key
//     */
//    public static String getPublicKeyStringBycert(String path) {
//        try {
//            SmCert smCert = (SmCert) FileUtils.readFile(path);
//            return Base64.getEncoder().encodeToString(smCert.getPublicKey());
//        } catch (Exception e) {
//            e.printStackTrace();
//            return null;
//        }
//    }
//
//}
