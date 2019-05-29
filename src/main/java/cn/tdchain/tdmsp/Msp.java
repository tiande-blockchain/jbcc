/*
 * Copyright (c) 2017 Beijing Tiande Technology Co., Ltd.
 * All Rights Reserved.
 */
package cn.tdchain.tdmsp;

import cn.tdchain.tdmsp.ca.config.SystemConfig;
import cn.tdchain.tdmsp.ca.config.TdMSPMsg;
import cn.tdchain.tdmsp.ca.root.RsaCertificate;
import cn.tdchain.tdmsp.manage.TdMSPIdentity;
import cn.tdchain.tdmsp.util.PkiConstant;
import cn.tdchain.tdmsp.util.PkiUtil;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.io.ByteArrayInputStream;
import java.security.KeyStore;
import java.security.Security;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Base64;

/**
 * Description:会员管理主程序
 * @author xiaoming
 * 2019年4月18日
 */
public class Msp {
    static {
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    /**
     * Description 根证书默认的别名
     */
    public static String ROOT_ALIAS = "root_ca_cert";

    public static String ORGANIZATION_ALIAS = "org_ca_alias";

    public static String COMMON_NAME = "td_common_name";

    /**
     * Description smp底层密钥算法类型 SM：国密算法   RSA：国际rsa密钥算法
     */
    private static Type type = Type.RSA;

    /**
     * Description: 创建跟证书
     * @param rootKsPath
     * @param rootPasswd
     */
    public static void createRootCert(String rootKsPath, String rootPasswd) {
        if (Msp.type == Type.RSA) {
            //rsa跟证书创建
            RsaCertificate rsa = new RsaCertificate();
            rsa.creatRootCA(rootKsPath, rootPasswd);
        } else {
            //sm跟证书创建

        }

    }

    /**
     * Description: 根据根证书创建组织证书
     * @param rootKsPath
     * @param rootPasswd
     * @param organizationUnit
     * @param certKsPath
     * @param certPassword
     */
    public static void createOrganizationUnitCert(String rootKsPath, String rootPasswd, String organizationUnit, String certKsPath, String certPassword) {
        if (Msp.type == Type.RSA) {
            //rsa组织证书创建
            RsaCertificate rsa = new RsaCertificate();
            SystemConfig keyStoreParam = new SystemConfig(rootKsPath, rootPasswd, organizationUnit, COMMON_NAME, ORGANIZATION_ALIAS, certKsPath, certPassword);

            rsa.creatOuCert(keyStoreParam);
        } else {
            //sm组织证书创建

        }
    }

    /**
     * Description: 验证是否是跟证书颁发的组织证书？
     * @param rootCert
     * @param cert
     * @return boolean
     */
    public static boolean validateCert(X509Certificate rootCert, X509Certificate cert) {
        TdMSPIdentity identity = new TdMSPIdentity();
        TdMSPMsg tdMSPMsg = identity.validateCert(rootCert, cert);
        if (tdMSPMsg.getType() == 0) {
            return true;//是跟证书颁发的证书
        } else {
            return false;
        }
    }

    /**
     * Description: 获取keystore对象
     * @param filePath
     * @param ksPassword
     * @return KeyStore
     */
    public static KeyStore getKeyStore(String filePath, String ksPassword) {
        try {
            return PkiUtil.getKeyStore(filePath, ksPassword, PkiConstant.PKCS12);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Description:读取磁盘上的cert证书文件
     * @param certPath
     * @return X509Certificate
     */
    public static X509Certificate getCert(String certPath) {
        try {
            return PkiUtil.getCertFromCer(certPath);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Description: 证书对象转换base64字符串
     * @param cert
     * @return String
     */
    public static String certToBase64String(X509Certificate cert) {
        if (cert != null) {
            try {
                return Base64.getEncoder().encodeToString(cert.getEncoded());
            } catch (CertificateEncodingException e) {
                return null;
            }
        } else {
            return null;
        }
    }

    /**
     * Description: base64字符串形式的证书转换成证书对象。
     * @param certBase64String
     * @return X509Certificate
     */
    public static X509Certificate base64StringToCert(String certBase64String) {
        if (certBase64String != null && certBase64String.length() > 0) {
            try {
                byte[] cert_b = Base64.getDecoder().decode(certBase64String);

                CertificateFactory cf = CertificateFactory.getInstance(PkiConstant.X509, BouncyCastleProvider.PROVIDER_NAME);
                X509Certificate x509Cert = (X509Certificate) cf.generateCertificate(new ByteArrayInputStream(cert_b));
                return x509Cert;
            } catch (Exception e) {
                return null;
            }
        } else {
            return null;
        }
    }

    /**
     * Description: 底层使用RSA密钥算法
     */
    public static void useRSA() {
        Msp.type = Type.RSA;
    }

    /**
     * Description: 底层使用SM密钥算法
     */
    public static void useSM() {
        Msp.type = Type.SM;
    }
    
    /**
     * Description: 获取密钥算法类型
     * @return Msp.Type
     */

    public static Msp.Type getType() {
        return Msp.type;
    }

    public static String getOrganizationName(X509Certificate clientCert) {
        String subjectName = clientCert.getSubjectX500Principal().getName();
        subjectName = subjectName.split("OU=")[1];
        String substring = subjectName.substring(0, subjectName.indexOf(","));
        return substring;
    }

    public enum Type {
        SM, RSA
    }


    public static void main(String[] args) throws Exception {
        Msp.useRSA();
        // 1. 创建跟证书

//         Msp.createRootCert("/Users/yaochuang/cer/test/rsa_tdbc_root.pfx", "123456");
//

        // 2.根据跟证书 创建组织证书
//		Msp.createOrganizationUnitCert("E:\\keys\\rsa\\rsa_tdbc_root.pfx", "123456", "ou1", "ou1.test1", "ou1Cert", "E:\\keys\\rsa\\rsa_tiande.pfx", "123456");
//		Msp.createRootCert("/Users/yaochuang/cer/rsa_2_tdbc_root.pfx", "123456");


        // 2.根据跟证书 创建组织证书
        Msp.createOrganizationUnitCert("/Users/yaochuang/cer/test/rsa_tdbc_root.pfx", "123456", "user", "/Users/yaochuang/cer/test/rsa_user.pfx", "123456");


        // 3. 加密解密测试
//		KeyStore keyStore = PkiUtil.getKeyStore("E:\\keys\\rsa\\rsa_tdbc_root.pfx", "123456", PkiConstant.PKCS12);
//        PrivateKey privateKey = (PrivateKey) keyStore.getKey(ROOT_ALIAS, "123456".toCharArray());
//        PublicKey  publicKey =  ((X509Certificate) keyStore.getCertificate(ROOT_ALIAS)).getPublicKey();
//
//		Cipher cipher = new Cipher();
//		String ci_text = cipher.encryptByPublicKey("小明", publicKey);
//		System.out.println("ci_text=" + ci_text);
//		String data = cipher.decryptByPrivateKey(ci_text, privateKey);
//		System.out.println("data=" + data);

        //验证证书颁发
//		KeyStore root_keyStore = Msp.getKeyStore("E:\\keys\\rsa\\rsa_tdbc_root.pfx", "123456");
//		KeyStore tiande_keyStore = Msp.getKeyStore("E:\\keys\\rsa\\rsa_tiande.pfx", "123456");
//
//		PrivateKey privateKey = (PrivateKey) tiande_keyStore.getKey("ou1Cert", "123456".toCharArray());
//
//		X509Certificate root_cert = (X509Certificate) root_keyStore.getCertificate(ROOT_ALIAS);
//		X509Certificate tiande_cert = (X509Certificate) tiande_keyStore.getCertificate(ROOT_ALIAS);
//
//
//		byte[] bencoded = tiande_cert.getEncoded();
//
//
//		CertificateFactory cf = CertificateFactory.getInstance(PkiConstant.X509, BouncyCastleProvider.PROVIDER_NAME);
//	     X509Certificate x509Cert = (X509Certificate)cf.generateCertificate(new ByteArrayInputStream(bencoded));
//
//		boolean is = Msp.validateCert(root_cert, x509Cert);
//		System.out.println("is=" + is);
    }

}