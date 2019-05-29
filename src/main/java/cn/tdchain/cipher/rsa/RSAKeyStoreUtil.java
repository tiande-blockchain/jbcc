/*
 * Copyright (c) 2017 Beijing Tiande Technology Co., Ltd.
 * All Rights Reserved.
 */
package cn.tdchain.cipher.rsa;

import cn.tdchain.cipher.CipherException;
import cn.tdchain.cipher.utils.CipherUtil;
import cn.tdchain.jbcc.SoutUtil;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.io.File;
import java.io.FileInputStream;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.PublicKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * @author xiaoming
 * 2019年4月18日
 */
public class RSAKeyStoreUtil {

    private static final String KEY_STORE = "JKS";
    private static final String X509 = "X.509";
    private static Provider provider = new BouncyCastleProvider();

    private RSAKeyStoreUtil() {
    }

    /**
     * Get private key String.
     * 
     * @param path
     * @param alias
     * @param storePass
     * @return String
     * @throws Exception
     */
    public static String getPrivateKeyString(String path, String alias, String storePass) throws Exception {
        PrivateKey priKey = getPrivateKey(path, alias, storePass);
        return Base64.getEncoder().encodeToString(priKey.getEncoded());
    }

    /**
     * Get private key.
     * 
     * @param privateKeyStr
     * @return PrivateKey
     * @throws Exception
     */
    public static PrivateKey getPrivateKey(String privateKeyStr)
            throws Exception {
        byte[] keyBytes = Base64.getDecoder().decode(privateKeyStr);
        PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory
                .getInstance("RSA", provider);
        return keyFactory.generatePrivate(pkcs8KeySpec);
    }

    /**
     * Get public key String.
     *
     * @param path String
     * @return String
     * @throws Exception
     */
    public static String getPublicKeyStr(String path) throws Exception {
        X509Certificate cert = getCert(path);
        PublicKey publicKey = cert.getPublicKey();
        return Base64.getEncoder().encodeToString(publicKey.getEncoded());
    }

    /**
     * Get public key.
     *
     * @param publicKeyStr String
     * @return public key
     * @throws Exception
     */
    public static PublicKey getPublicKey(String publicKeyStr) throws Exception {
        byte[] keyBytes = Base64.getDecoder().decode(publicKeyStr);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory
                .getInstance("RSA", provider);
        return keyFactory.generatePublic(keySpec);
    }

    /**
     * Get certification.
     * 
     * @param path
     * @return X509Certificate
     * @throws Exception
     */
    public static X509Certificate getCert(String path) throws Exception {

        CertificateFactory certFactory = CertificateFactory.getInstance(X509);
        FileInputStream fis = null;
        X509Certificate cert = null;
        try {
            fis = new FileInputStream(path);
            cert = (X509Certificate) certFactory.generateCertificate(fis);
        } finally {
            if (fis != null) {
                fis.close();
            }
        }
        return cert;
    }

    private static KeyStore getKeyStore(String keyStorePath, String password)
            throws Exception {

        if (SoutUtil.isOpenSout()) {
            System.out.println("getPublicKeyStringByStore before password: " + password);
            System.out.println("getPublicKeyStringByStore keyStorePath: " + keyStorePath);
        }

        String pwd = CipherUtil.zeroSuffix(password);

        if (SoutUtil.isOpenSout())
            System.out.println("getPublicKeyStringByStore after pwd: " + pwd);

        FileInputStream is = null;
        KeyStore ks = null;
        try {
            is = new FileInputStream(keyStorePath);
            ks = KeyStore.getInstance(KEY_STORE);
            ks.load(is, pwd.toCharArray());
        } finally {
            if (is != null) {
                is.close();
            }
        }
        return ks;
    }

    /**
     * Generate key using keytool.
     *
     * @param path     String
     * @param password String
     * @param alias    String
     */
    public static void genKey(String path, String password, String alias) {
        /* 创建密钥库路径 */
        File ksPath = new File(path);
        ksPath.getParentFile().mkdirs();

        String pwd = CipherUtil.zeroSuffix(password);
        if (SoutUtil.isOpenSout()) {
            System.out.println("generateKeyStoreFile path:" + path);
            System.out.println("generateKeyStoreFile pwd:" + pwd);
        }

        String[] arstringCommand = new String[]{"keytool", "-genkey", // -genkey表示生成密钥
                "-validity", "36500", // -validity指定证书有效期(单位：天)，这里是36000天
                "-keysize", "1024", // 指定密钥长度
                "-alias", alias, // -alias指定别名，这里是ss
                "-keyalg", "RSA", // -keyalg 指定密钥的算法 (如 RSA DSA（如果不指定默认采用DSA）)
                "-keystore", path, // -keystore指定存储位置，这里是d:/demo.keystore
                "-dname", getDname(alias), // dname
                "-storepass", pwd, // 指定密钥库的密码(获取keystore信息所需的密码
                "-keypass", pwd, // 指定别名条目的密码(私钥的密码)
                "-v"// -v 显示密钥库中的证书详细信息
        };
        execCommand(arstringCommand);

    }

    private static String getDname(String alias) {
        StringBuilder dname = new StringBuilder();
        dname.append("CN=(").append(alias).append("), "); // CN=(名字与姓氏)
        dname.append("OU=(").append(alias).append("), "); // OU=(组织单位名称)
        dname.append("O=(").append(alias).append("), "); // O=(组织名称)
        dname.append("L=(BJ), ST=(BJ), C=(CN)"); // L=(城市或区域名称), ST=(州或省份名称), C=(单位的两字母国家代码)
        return dname.toString();
    }

    /**
     * Generate certification.
     *
     * @param path     String
     * @param password String
     * @param alias    String
     */
    public static void genCert(String path, String password, String alias) {
        String pwd = CipherUtil.zeroSuffix(password);
        String[] arstringCommand = new String[]{"keytool", "-export",
                "-alias", alias, "-keystore", path, "-file", path + ".cert",
                "-storepass", pwd};

        execCommand(arstringCommand);
    }


    private static void execCommand(String[] arstringCommand) {
        try {
            Runtime.getRuntime().exec(arstringCommand);
        } catch (Exception e) {
            throw new CipherException("exec command error : " + e.getMessage());
        }
    }

    public static PublicKey getPublicKeyByCert(String certPath) throws Exception {
        X509Certificate cert = getCert(certPath);
        return cert.getPublicKey();
    }


    public static String getPublicKeyStringByKeyStore(String ksPath, String alias, String ksPass) throws Exception {
        PublicKey publicKey = getPublicKeyByKeyStore(ksPath, alias, ksPass);

        return Base64.getEncoder().encodeToString(publicKey.getEncoded());
    }

    public static PublicKey getPublicKeyByKeyStore(String ksPath, String alias, String ksPass) throws Exception {
        String pwd = CipherUtil.zeroSuffix(ksPass);
        KeyStore ks = getKeyStore(ksPath, pwd);
        X509Certificate cert = (X509Certificate) ks.getCertificate(alias);
        return cert.getPublicKey();
    }

    public static PrivateKey getPrivateKey(String path, String alias, String storePass) throws Exception {
        String pwd = CipherUtil.zeroSuffix(storePass);
        KeyStore ks = getKeyStore(path, pwd);
        PrivateKey priKey = (PrivateKey) ks.getKey(alias,
                pwd.toCharArray());
        return priKey;
    }

}
