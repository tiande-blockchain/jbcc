/*
 * Copyright (c) 2019 Beijing Tiande Technology Co., Ltd.
 * All Rights Reserved.
 */
package cn.tdchain.tdmsp;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;

import javax.security.auth.x500.X500Principal;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.Feature;
import com.alibaba.fastjson.serializer.SerializerFeature;

import cn.tdchain.cipher.rsa.RsaUtil;
import cn.tdchain.cipher.sm.Sm2Util;
import cn.tdchain.tdmsp.ca.config.KeyStoreParam;
import cn.tdchain.tdmsp.ca.config.MspConfig;
import cn.tdchain.tdmsp.ca.config.PoliciesRule;
import cn.tdchain.tdmsp.ca.config.PoliciesType;
import cn.tdchain.tdmsp.ca.config.TdMSPMsg;
import cn.tdchain.tdmsp.ca.root.EccCertificate;
import cn.tdchain.tdmsp.ca.root.RsaCertificate;
import cn.tdchain.tdmsp.ca.root.Sm2Certificate;
import cn.tdchain.tdmsp.manage.Policies;
import cn.tdchain.tdmsp.manage.TdMSPAcl;
import cn.tdchain.tdmsp.manage.TdMSPIdentity;
import cn.tdchain.tdmsp.manage.TdMSPManage;
import cn.tdchain.tdmsp.util.EccUtil;
import cn.tdchain.tdmsp.util.IOUtils;
import cn.tdchain.tdmsp.util.PkiConstant;
import cn.tdchain.tdmsp.util.PkiUtil;

/**
 *
 *
 * @version 1.0
 * @author jiating 2019-01-08
 */
public class MSPTest {

//    protected static final  Logger log = LoggerFactory.getLogger(MSPTest.class);
    private  MspConfig mspConfig = null;
    private  TdMSPAcl tdMSPAcl  = null;
    
    static {
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }
    
    public static void main(String[] args) throws IOException {
        
//        saveTojson();
//        readTojson();
        createRSARootCert();//创建RSA根证书
//          createRSACert();   //生成RSA组织证书   
//          signMsg("this is a test message");//RSA签名测试
//          verifyMsg();//RSA签验证
//           testRSA_encryptDecrypt();//RSA加密解密测试
//        testCert();//
//        testTdMSPIdentity();//RSA类型的会员管理权限验证
//        createSM2ootCert();//创建国密根证书
//        createSM2Cert(); //生成国密组织证书 
//          testSM2_encryptDecrypt();//国密加密解密测试
//          testSM2Sign_Verify();//国密签名和会员管理权限验证
//           createECCrootCert();//创建ECC根证书
//           createECCCert(); //生成ECC组织证书 
//            testECCSign_Verify();//ECC签名和会员管理权限验证
//        testEcc();//
//          testMSPManage();
        
     
//        System.out.println(JSON.toJSONString(object));
        
    }
    
  
   
  
    private static void testMSPManage() {
        MspConfig mspConfig =  TdMSPManage.initLocalMsp();
        TdMSPAcl tdMSPAcl =  TdMSPManage.initMspAcl();
        
        boolean result =  TdMSPManage.checkCert(mspConfig);
        
//        log.debug("mspConfig {}",mspConfig);
//        log.debug("tdMSPAcl {}",tdMSPAcl);
//        log.debug("result {}",result);
        
    }



    private static void testEcc() {
        try {
            String msg = "this is a test";
            KeyPair  keyPair =  PkiUtil.generateEccKeyPair();
            String signMsg = EccUtil.sign(msg, keyPair.getPrivate());
            
           boolean result =  EccUtil.verify(msg, signMsg, keyPair.getPublic());
            
            
//            log.debug("signMsg {}",signMsg);
//            log.debug("v {}",result);
            
        } catch (Exception e) {
//            log.error(e.getMessage(), e);
        } 
    }


    private static void testECCSign_Verify() {
        String msg = "this is a test message for ecc";
        String signMsg = signMsgByECC(msg);

        String method = "getBlock";
        
        String rootPath = "./msp/ca/ou/ecc/rootCert.cer";
        String certPath = "./msp/ca/ou/ecc/ouCert.cer";
     
        X509Certificate rootCert = PkiUtil.getCertFromCer(rootPath);
        X509Certificate cert = PkiUtil.getCertFromCer(certPath);
        
        TdMSPAcl tdMSPAcl =  readTojson();
        
        TdMSPIdentity identity = new TdMSPIdentity();
        TdMSPMsg tdmsg = identity.validate(msg, signMsg, cert, method, tdMSPAcl, rootCert,PkiConstant.ECC);

//        log.debug("tdmsg {}",JSON.toJSONString(tdmsg));   
    }




    private static void createECCCert() {
        EccCertificate ecc = new EccCertificate();
        KeyStoreParam keyStoreParam = getEccKeyStoreParam();
        
        ecc.creatOuCert(keyStoreParam);
//        log.info("createECCCert end");
        
    }




   
    private static KeyStoreParam getEccKeyStoreParam() {
        KeyStoreParam keyStoreParam = new KeyStoreParam();
        
        keyStoreParam.setCountry("CN");
        keyStoreParam.setOrganizationUnit("eccOU1");
        keyStoreParam.setCommonName("eccOU1.test1");
        
        keyStoreParam.setPrivateKeyPassword("1qaz");
        keyStoreParam.setPrivateKeyAlias("test1_privateKey");
        
        keyStoreParam.setCertAlias("ou1Cert");
        keyStoreParam.setRootAlias("rootCert");
        keyStoreParam.setValidTime(5);
        
        keyStoreParam.setKsPassword("123456");
        
        keyStoreParam.setPath("./msp/ca/ou/ecc/");
        keyStoreParam.setFileName("eccOU1.pfx");
                
        return keyStoreParam;
    }




    /**
     * 
     */
    private static void createECCrootCert() {
        EccCertificate ecc = new EccCertificate();
        ecc.creatRootCA();
        
//        log.info("createECCrootCert end");
        
    }




    private static void testSM2Sign_Verify() {
        String msg = "this is a test message for sm2";
        String signMsg = signMsgBySM2(msg);
        
        String method = "addTrans";
        
        String rootPath = "./msp/ca/ou/sm2/rootCert.cer";
        String certPath = "./msp/ca/ou/sm2/ouCert.cer";
     
        X509Certificate rootCert = PkiUtil.getCertFromCer(rootPath);
        X509Certificate cert = PkiUtil.getCertFromCer(certPath);
        
        TdMSPAcl tdMSPAcl =  readTojson();
        
        TdMSPIdentity identity = new TdMSPIdentity();
        TdMSPMsg tdmsg = identity.validate(msg, signMsg, cert, method, tdMSPAcl, rootCert,PkiConstant.SM2);

//        log.debug("tdmsg {}",JSON.toJSONString(tdmsg));
        
    }

    private static String signMsgByECC(String msg) {
        String signMsg  = null;
        
        String keyStorePath = "./msp/ca/ou/ecc/eccOU1.pfx";
        String keyStorePass = "123456";
        
//        log.debug("msg {}",msg);
        
        try {
            KeyStore keyStore = PkiUtil.getKeyStore(keyStorePath, keyStorePass, PkiConstant.PKCS12);
            X509Certificate  cert =  (X509Certificate) keyStore.getCertificate("ou1Cert");
            PrivateKey privateKey = (PrivateKey) keyStore.getKey("test1_privateKey", "1qaz".toCharArray());
            
            signMsg  = EccUtil.sign(msg, privateKey);
            boolean verifyResult = EccUtil.verify(msg, signMsg, cert.getPublicKey());
            
//            log.debug("signMsg {}",signMsg);
//            log.debug("verifyResult {}",verifyResult);
            
        } catch (Exception e) {
//            log.error(e.getMessage(), e);
        }
        
        return signMsg;
    }

   
    private static String signMsgBySM2(String msg) {
        
        String signMsg  = null;
        
        String keyStorePath = "./msp/ca/ou/sm2/sm2OU1.pfx";
        String keyStorePass = "123456";
        
//        log.debug("msg {}",msg);
        
        try {
            KeyStore keyStore = PkiUtil.getKeyStore(keyStorePath, keyStorePass, PkiConstant.PKCS12);
            X509Certificate  cert =  (X509Certificate) keyStore.getCertificate("ou1Cert");
            PrivateKey privateKey = (PrivateKey) keyStore.getKey("test1_privateKey", "1qaz".toCharArray());
           
            signMsg  = Sm2Util.sign(privateKey, msg);
            boolean verifyResult = Sm2Util.verify(cert.getPublicKey(),signMsg, msg );
            
//            log.debug("signMsg {}",signMsg);
//            log.debug("verifyResult {}",verifyResult);
            
        } catch (Exception e) {
//            log.error(e.getMessage(), e);
        }
        
        return signMsg;
    }




    private static void testSM2_encryptDecrypt() {
        String msg = "Today is January 14, 2019 for sm2";
        String keyStorePath = "./msp/ca/ou/sm2/sm2OU1.pfx";
        String keyStorePass = "123456";
        
//        log.debug("msg {}",msg);
        
        try {
            KeyStore keyStore = PkiUtil.getKeyStore(keyStorePath, keyStorePass, PkiConstant.PKCS12);
            X509Certificate  cert =  (X509Certificate) keyStore.getCertificate("ou1Cert");
            PrivateKey privateKey = (PrivateKey) keyStore.getKey("test1_privateKey", "1qaz".toCharArray());
           
            String encryptMsg =  Sm2Util.encrypt(cert.getPublicKey(),msg);
            String decryptMsg = Sm2Util.decrypt(privateKey,encryptMsg);
            
            
//            log.debug("encryptMsg {}",encryptMsg);
//            log.debug("decryptMsg {}",decryptMsg);
//            log.debug("decryptMsg text {}",new String(Base64Utils.decode(decryptMsg)));
            
        } catch (Exception e) {
//            log.error(e.getMessage(), e);
                
        }
        
    }


   
    private static void testRSA_encryptDecrypt() {
        String msg = "Today is January 14, 2019";
        String keyStorePath = "./msp/ca/ou/test1.pfx";
        String keyStorePass = "123456";
        
//        log.debug("msg {}",msg);
        
        try {
            KeyStore keyStore = PkiUtil.getKeyStore(keyStorePath, keyStorePass, PkiConstant.PKCS12);
            X509Certificate  cert =  (X509Certificate) keyStore.getCertificate("ou1Cert");
            PrivateKey privateKey = (PrivateKey) keyStore.getKey("test1_privateKey", "1qaz".toCharArray());
           
            
            String encryptMsg =  RsaUtil.encrypt(msg,cert.getPublicKey());
            String decryptMsg = RsaUtil.decrypt(encryptMsg,privateKey);
            
            
//            log.debug("encryptMsg {}",encryptMsg);
//            log.debug("decryptMsg {}",decryptMsg);
            
        } catch (Exception e) {
//            log.error(e.getMessage(), e);
                
        }
        
       
    }


    private static void createSM2Cert() {
        Sm2Certificate sm2 = new Sm2Certificate();
        KeyStoreParam keyStoreParam = getSM2KeyStoreParam();
        
        sm2.creatOuCert(keyStoreParam);
//        log.info("createSM2Cert end");
    }


    private static KeyStoreParam getSM2KeyStoreParam() {
        KeyStoreParam keyStoreParam = new KeyStoreParam();
        
        keyStoreParam.setCountry("CN");
        keyStoreParam.setOrganizationUnit("sm2OU1");
        keyStoreParam.setCommonName("sm2OU1.test1");
        
        keyStoreParam.setPrivateKeyPassword("1qaz");
        keyStoreParam.setPrivateKeyAlias("test1_privateKey");
        
        keyStoreParam.setCertAlias("ou1Cert");
        keyStoreParam.setRootAlias("rootCert");
        keyStoreParam.setValidTime(5);
        
        keyStoreParam.setKsPassword("123456");
        
        keyStoreParam.setPath("./msp/ca/ou/sm2/");
        keyStoreParam.setFileName("sm2OU1.pfx");
                
        return keyStoreParam;
    }


    /**
     * 
     */
    private static void createSM2ootCert() {
        Sm2Certificate sm2 = new Sm2Certificate();
        sm2.creatRootCA();
        
//        log.info("createSM2ootCert end");
    }


    private static void testTdMSPIdentity() {
        String msg = "this is a test message";
        String signMsg = signMsg(msg);
        
        String method = "addTrans";
        
        String rootPath = "./msp/ca/ou/rootCert.cer";
        String certPath = "./msp/ca/ou/ouCert.cer";
     
        X509Certificate rootCert = PkiUtil.getCertFromCer(rootPath);
        X509Certificate cert = PkiUtil.getCertFromCer(certPath);
        
        TdMSPAcl tdMSPAcl =  readTojson();
        
        TdMSPIdentity identity = new TdMSPIdentity();
        TdMSPMsg tdmsg = identity.validate(msg, signMsg, cert, method, tdMSPAcl, rootCert,PkiConstant.RSA);

//        log.debug("tdmsg {}",JSON.toJSONString(tdmsg));
        
        
        
    }

 


    private static void testCert() {
        String keyStorePath = "./msp/ca/ou/test1.pfx";
        String keyStorePass = "123456";
        
        String rootCert = "./msp/ca/ou/rootCert.cer";
        
        try {
            KeyStore keyStore = PkiUtil.getKeyStore(keyStorePath, keyStorePass, PkiConstant.PKCS12);
            X509Certificate  cert =  (X509Certificate) keyStore.getCertificate("ou1Cert");
            
            X500Principal  principal =  cert.getSubjectX500Principal();
           String str =  principal.getName();
           str = str.split("OU=")[1];
           str = str.substring(0, str.indexOf(","));
            
//            log.info("name {}",str);
            
           byte[] certByte =  IOUtils.getBytes(rootCert);
           ByteArrayInputStream inStream = new ByteArrayInputStream(certByte);
           
           X509Certificate rootCA = (X509Certificate) CertificateFactory
                       .getInstance(PkiConstant.X509,
                               BouncyCastleProvider.PROVIDER_NAME)
                       .generateCertificate(inStream);
           
//           log.info("rootCA {}",rootCA.getIssuerX500Principal().getName());
            
        } catch (Exception e) {
//            log.error(e.getMessage(), e);
                
        }
    }



    /**
     * 
     */
    private static void verifyMsg() {
        String msg = "this is a test message";
        String signMsg = signMsg(msg);
        String keyStorePath = "./msp/ca/ou/test1.pfx";
        String keyStorePass = "123456";
        
        try {
            KeyStore keyStore = PkiUtil.getKeyStore(keyStorePath, keyStorePass, PkiConstant.PKCS12);
            X509Certificate  cert =  (X509Certificate) keyStore.getCertificate("ou1Cert");
            
            boolean verifyRsult =  RsaUtil.verify(cert.getPublicKey(), signMsg, msg);
//            log.debug("verifyRsult {}",verifyRsult);
            
        } catch (Exception e) {
//            log.error(e.getMessage(), e);
                
        }
        
       
    }



    private static void createRSARootCert() {
        RsaCertificate rsa = new RsaCertificate();
        rsa.creatRootCA();
//        log.info("createRSARootCert end");
        
    }

    private static void createRSACert() {
        RsaCertificate rsa = new RsaCertificate();
        KeyStoreParam keyStoreParam = getKeyStoreParam();
        
        rsa.creatOuCert(keyStoreParam);
//        log.info("createRSACert end");
        System.out.println("createRSACert end");
    }


    /**
     * @return
     */
    private static KeyStoreParam getKeyStoreParam() {
        KeyStoreParam keyStoreParam = new KeyStoreParam();
       
        keyStoreParam.setCountry("CN");
        keyStoreParam.setOrganizationUnit("testOU1");
        keyStoreParam.setCommonName("testOU1.test1");
        
        keyStoreParam.setPrivateKeyPassword("1qaz");
        keyStoreParam.setPrivateKeyAlias("test1_privateKey");
        
        keyStoreParam.setCertAlias("ou1Cert");
        keyStoreParam.setRootAlias("rootCert");
        keyStoreParam.setValidTime(5);
        
        keyStoreParam.setKsPassword("123456");
        
        keyStoreParam.setPath("./msp/ca/ou/");
        keyStoreParam.setFileName("test1.pfx");
                
        return keyStoreParam;
    }



    private static String signMsg(String msg) {
        
//        String msg = "this is a test message";
        String keyStorePass = "123456";
        String privateKeyAlias = "test1_privateKey";
        String privateKeyPassword = "1qaz";
        String signMsg = "";
        
        String keyStorePath = "./msp/ca/ou/test1.pfx";
        try {
            KeyStore keyStore = PkiUtil.getKeyStore(keyStorePath, keyStorePass, PkiConstant.PKCS12);
            PrivateKey privateKey = (PrivateKey) keyStore.getKey(privateKeyAlias, privateKeyPassword.toCharArray());
            signMsg = RsaUtil.sign(privateKey, msg);
            
        } catch (Exception e) {
//            log.error(e.getMessage(), e);
            	
        }
//        log.debug(signMsg);
        
        return signMsg;
        
    }

    private static TdMSPAcl readTojson() {
        String path = "./src/main/resources/acl.json";
        byte[] jsonByte =  IOUtils.getBytes(path);
        
        TdMSPAcl tdMSPAcl = JSON.parseObject(jsonByte, TdMSPAcl.class, Feature.IgnoreAutoType);
        
        System.out.println(JSON.toJSONString(tdMSPAcl));
//        System.out.println(tdMSPAcl.getAclList().get(0).getAclName());
        return tdMSPAcl;
    }

    private static void saveTojson() {
        String path = "./src/main/resources/acl.json";
        TdMSPAcl tdMSPAcl = new TdMSPAcl();
        ArrayList<String> crlList = new ArrayList<String>();
        ArrayList<String> ouList = new ArrayList<String>();
        ArrayList<Policies> policiesList = new ArrayList<Policies>();
        HashMap<String , ArrayList<String>> aclMap = new HashMap<String , ArrayList<String>>();
        
        crlList.add("11111000001");
        crlList.add("22222000002");
        tdMSPAcl.setCrlList(crlList);
        
        ouList.add("ou1");
        ouList.add("ou2");
        tdMSPAcl.setOuList(ouList);
        
        policiesList = getPoliciesList();
        tdMSPAcl.setPoliciesList(policiesList);
        
        aclMap = getAclMap();
        tdMSPAcl.setAclMap(aclMap);
        
        try {
            File file = new File(path);
            if(file.exists()) {
                file.delete();
            }
            
            FileOutputStream fos = new FileOutputStream(file);
            String tdMSPAclStr = JSON.toJSONString(tdMSPAcl);
            
            JSON.writeJSONString(fos, tdMSPAclStr, SerializerFeature.EMPTY);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
//        log.debug("saveTojson sucess");
    }

    private static HashMap<String , ArrayList<String>> getAclMap() {
        
        HashMap<String , ArrayList<String>> hashMap = new HashMap<String , ArrayList<String>>();
        
        ArrayList<String> aclValues = new ArrayList<String>();
        aclValues.add("Writes");
        hashMap.put("addTrans", aclValues);
        
        aclValues = new ArrayList<String>();
        aclValues.add("Reads");
        aclValues.add("Writes");
        hashMap.put("getBlock", aclValues);
        
        aclValues = new ArrayList<String>();
        aclValues.add("Reads");
        aclValues.add("Writes");
        hashMap.put("getMaxBlock", aclValues);
        
        aclValues = new ArrayList<String>();
        aclValues.add("Reads");
        aclValues.add("Writes");
        hashMap.put("getTransByHash", aclValues);
        
        aclValues = new ArrayList<String>();
        aclValues.add("Reads");
        aclValues.add("Writes");
        hashMap.put("getTransListByHashList", aclValues);
        
        aclValues = new ArrayList<String>();
        aclValues.add("Reads");
        aclValues.add("Writes");
        hashMap.put("getNewTransByKey", aclValues);
        
        aclValues = new ArrayList<String>();
        aclValues.add("Reads");
        aclValues.add("Writes");
        hashMap.put("getTransHistoryByKey", aclValues);
        
        aclValues = new ArrayList<String>();
        aclValues.add("Admins");
        hashMap.put("getACLMsp", aclValues);
        
        aclValues = new ArrayList<String>();
        aclValues.add("Admins");
        hashMap.put("updateACLMsp", aclValues);
        
        return hashMap;
    }




    /**
     * @return
     */
    private static ArrayList<Policies> getPoliciesList() {
        ArrayList<Policies> policiesList = new ArrayList<Policies>();
        
        Policies policies = new Policies();
        ArrayList<String> list = new ArrayList<String>();
        policies.setPoliciesName("Reads");
        policies.setPoliciesRule(PoliciesRule.OR);
        policies.setPoliciesType(PoliciesType.Signature);
        list.add("sale");
        list.add("purchase");
        list.add("logistics");
        policies.setPoliciesList(list);
        policiesList.add(policies);
        
        policies = new Policies();
        list = new ArrayList<String>();
        policies.setPoliciesName("Writes");
        policies.setPoliciesRule(PoliciesRule.OR);
        policies.setPoliciesType(PoliciesType.Signature);
        list.add("development");
        list.add("finance");
        policies.setPoliciesList(list);
        policiesList.add(policies);
        
        policies = new Policies();
        list = new ArrayList<String>();
        policies.setPoliciesName("Admins");
        policies.setPoliciesRule(PoliciesRule.OR);
        policies.setPoliciesType(PoliciesType.Signature);
        list.add("admin1");
        list.add("admin2");
        policies.setPoliciesList(list);
        policiesList.add(policies);
        
        return policiesList;
    }

    
    
    public MspConfig getMspConfig() {
        return mspConfig;
    }

    public TdMSPAcl getTdMSPAcl() {
        return tdMSPAcl;
    }

    public  void initMspAcl() {
        String path = "./src/main/resources/acl.json";
        byte[] jsonByte =  IOUtils.getBytes(path);
        
       tdMSPAcl = JSON.parseObject(jsonByte, TdMSPAcl.class, Feature.IgnoreAutoType);
//        log.debug("initMspAcl {}",JSON.toJSONString(tdMSPAcl));
    }


    public  void initLocalMsp() {
        mspConfig =  MspConfig.getInstance();
//        log.debug("init local msp {}",JSON.toJSONString(mspConfig));
    }

}
