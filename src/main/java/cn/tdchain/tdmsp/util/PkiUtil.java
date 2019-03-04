/*
 * Copyright (c) 2017-2018 Beijing Tiande Technology Co., Ltd.
 * All Rights Reserved.
 */
package cn.tdchain.tdmsp.util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.ECGenParameterSpec;
import java.util.Calendar;
import java.util.Date;

import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.DERGeneralString;
import org.bouncycastle.asn1.oiw.OIWObjectIdentifiers;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.X500NameBuilder;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.ExtendedKeyUsage;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.KeyPurposeId;
import org.bouncycastle.asn1.x509.KeyUsage;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509ExtensionUtils;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECNamedCurveGenParameterSpec;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.DigestCalculator;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.bc.BcDigestCalculatorProvider;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.bouncycastle.pkcs.PKCS10CertificationRequestBuilder;
import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequestBuilder;

import cn.tdchain.tdmsp.ca.config.KeyStoreParam;

/**
 * Utility.
 *
 * @version 2.0
 * @author Houmj 2018-09-18
 */
public final class PkiUtil {

    static {
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    private PkiUtil() {
    }

    /**
     * Create a random 1024 bit RSA key pair.
     * 
     * @throws NoSuchProviderException exception
     * @throws NoSuchAlgorithmException exception
     * @return keypair
     */
    public static KeyPair generateRsaKeyPair()
        throws NoSuchAlgorithmException, NoSuchProviderException {
        KeyPairGenerator kpGen = KeyPairGenerator.getInstance(PkiConstant.RSA,
                BouncyCastleProvider.PROVIDER_NAME);
        kpGen.initialize(1024, new SecureRandom());

        return kpGen.generateKeyPair();
    }

    /**
     * Generate SM2 keys.
     * 
     * @return KeyPair
     * @throws Exception NoSuchAlgorithmException, NoSuchProviderException,
     *             InvalidAlgorithmParameterException
     */
    public static KeyPair generateSm2KeyPair() throws Exception {
        KeyPairGenerator g = KeyPairGenerator.getInstance(PkiConstant.EC,
                BouncyCastleProvider.PROVIDER_NAME);
        g.initialize(new ECNamedCurveGenParameterSpec(PkiConstant.SM2P256V1),
                new SecureRandom());
        return g.generateKeyPair();
    }

    /**
     * Generate ECC key pair.
     * <p>
     * Private keypairs are encoded using PKCS8 Private keys are encoded using X.509
     * </p>
     * 
     * @return ECC KeyPair
     * @throws InvalidAlgorithmParameterException This is the exception for invalid or inappropriate
     *             algorithm parameters.
     * @throws NoSuchAlgorithmException This exception is thrown when a particular cryptographic
     *             algorithm is requested but is not available in the environment.
     * @throws NoSuchProviderException This exception is thrown when a particular security provider
     *             is requested but is not available in the environment.
     */
    public static KeyPair generateEccKeyPair()
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
     * Create encryption key pair.
     * 
     * @return encryption key pair
     * @throws NoSuchAlgorithmException exception
     * @throws NoSuchProviderException exception
     * @throws InvalidAlgorithmParameterException exception
     */
    public static KeyPair generateEcEncryptKeyPair()
        throws NoSuchAlgorithmException, NoSuchProviderException,
        InvalidAlgorithmParameterException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(
                PkiConstant.EC, BouncyCastleProvider.PROVIDER_NAME);
        keyPairGenerator.initialize(
                ECNamedCurveTable.getParameterSpec(PkiConstant.SECP256K1));
        return keyPairGenerator.generateKeyPair();
    }

    /**
     * Get X500Name.
     * 
     * @param name user full name
     * @param company String
     * @param country String
     * @return X500Name
     */
    public static X500Name getNameBuilder(String name, String company,
                                          String country) {
        X500NameBuilder nameBuilder = new X500NameBuilder(BCStyle.INSTANCE);
        nameBuilder.addRDN(BCStyle.CN, name);
        nameBuilder.addRDN(BCStyle.O, company);
        nameBuilder.addRDN(BCStyle.C, country);
        return nameBuilder.build();
    }
    
    public static X500Name getOuNameBuilder(String name, String company,
                                          String country) {
        X500NameBuilder nameBuilder = new X500NameBuilder(BCStyle.INSTANCE);
        nameBuilder.addRDN(BCStyle.CN, name);
        nameBuilder.addRDN(BCStyle.OU, company);
        nameBuilder.addRDN(BCStyle.C, country);
        return nameBuilder.build();
    }
    
    public static byte[] getRootEncodedHolder(SubjectPublicKeyInfo subPubKeyInfo,
                                          X500Name x500Name, int validTime,
                                          X500Name subject,
                                          PrivateKey privateKey,
                                          String algorithm)
        throws Exception {

        BigInteger serialNumber = BigInteger
                .valueOf(System.currentTimeMillis());

        Calendar c = Calendar.getInstance();
        Date startDate = c.getTime();
        c.add(Calendar.YEAR, validTime);
        Date endDate = c.getTime();

        X509v3CertificateBuilder x509v3CertBuilder = new X509v3CertificateBuilder(
                x500Name, serialNumber, startDate, endDate, subject,
                subPubKeyInfo);
        // Basic constraints is a CA
        BasicConstraints constraints = new BasicConstraints(true);
        x509v3CertBuilder.addExtension(
                Extension.basicConstraints,
                true,
                constraints.getEncoded());
       // Key usage
        KeyUsage usage = new KeyUsage(KeyUsage.keyEncipherment | KeyUsage.digitalSignature | KeyUsage.keyCertSign | KeyUsage.cRLSign);
        x509v3CertBuilder.addExtension(Extension.keyUsage, true, usage.getEncoded());
     // Extended key usage
        x509v3CertBuilder.addExtension(Extension.extendedKeyUsage, false,
                new ExtendedKeyUsage(KeyPurposeId.anyExtendedKeyUsage));
        // AuthorityKeyIdentifier,subjectKeyIdentifier 便于证书链的匹配查找 
        DigestCalculator calculator = new BcDigestCalculatorProvider().get(new AlgorithmIdentifier(OIWObjectIdentifiers.idSHA1));
        X509ExtensionUtils extensionUtils = new X509ExtensionUtils(calculator);
        x509v3CertBuilder.addExtension(Extension.subjectKeyIdentifier, false, extensionUtils.createSubjectKeyIdentifier(subPubKeyInfo));

        X509CertificateHolder holder = x509v3CertBuilder.build(getSigner(privateKey, algorithm));
               
        return holder.getEncoded();
    }

    /**
     * Get encoded X509CertificateHolder.
     * 
     * @param subPubKeyInfo SubjectPublicKeyInfo
     * @param x500Name X500Name
     * @param validTime cert valid time by year
     * @param subject X500Name
     * @param privateKey PrivateKey
     * @param algorithm SHA256withRSA/SHA256withECDSA/SM3withSM2
     * @return encoded X509CertificateHolder
     * @throws Exception exception
     */
    public static byte[] getEncodedHolder(SubjectPublicKeyInfo subPubKeyInfo,
                                          X500Name x500Name, int validTime,
                                          X500Name subject,
                                          PrivateKey privateKey,
                                          String algorithm,
                                          PublicKey rootPublickey)
        throws Exception {

        BigInteger serialNumber = BigInteger
                .valueOf(System.currentTimeMillis());

        Calendar c = Calendar.getInstance();
        Date startDate = c.getTime();
        c.add(Calendar.YEAR, validTime);
        Date endDate = c.getTime();

        X509v3CertificateBuilder x509v3CertBuilder = new X509v3CertificateBuilder(
                x500Name, serialNumber, startDate, endDate, subject,
                subPubKeyInfo);
     // Basic constraints not a CA,just one end cert
        BasicConstraints constraints = new BasicConstraints(false);
        x509v3CertBuilder.addExtension(
                Extension.basicConstraints,
                true,
                constraints.getEncoded());
       // Key usage
        KeyUsage usage = new KeyUsage(KeyUsage.dataEncipherment | KeyUsage.digitalSignature  );
        x509v3CertBuilder.addExtension(Extension.keyUsage, true, usage.getEncoded());
     
        // AuthorityKeyIdentifier 颁发者密钥标识,subjectKeyIdentifier 使用者密钥标识  便于证书链的匹配查找 
        DigestCalculator calculator = new BcDigestCalculatorProvider().get(new AlgorithmIdentifier(OIWObjectIdentifiers.idSHA1));
        X509ExtensionUtils extensionUtils = new X509ExtensionUtils(calculator);
        SubjectPublicKeyInfo rootPubKeyInfo = SubjectPublicKeyInfo
                .getInstance(rootPublickey.getEncoded());
        x509v3CertBuilder.addExtension(Extension.authorityKeyIdentifier, false, extensionUtils.createAuthorityKeyIdentifier(rootPubKeyInfo));
        
        X509CertificateHolder holder = x509v3CertBuilder
                .build(getSigner(privateKey, algorithm));

        return holder.getEncoded();
    }

    /**
     * Create E-cert request.
     * 
     * @param keyPair KeyPair
     * @param algorithm SHA256withRSA/SHA256withECDSA/SM3withSM2
     * @param name user full name
     * @param company user company
     * @param country user country
     * @return PKCS10CertificationRequest
     * @throws Exception exception
     */
    public static PKCS10CertificationRequest generateEnrollRequest(KeyPair keyPair,
                                                                   String algorithm,
                                                                   String name,
                                                                   String company,
                                                                   String country)
        throws Exception {

        PKCS10CertificationRequestBuilder p10Builder = new JcaPKCS10CertificationRequestBuilder(
                getNameBuilder(name, company, country), keyPair.getPublic());
        return p10Builder.build(getSigner(keyPair.getPrivate(), algorithm));
    }
    
    public static PKCS10CertificationRequest generateEccEncryptEnrollCARequest(KeyPair keyPair,
                                                                               String country,
                                                                               String company,
                                                                               String name)
                  throws Exception {

                  PKCS10CertificationRequest csr = null;

                  X500NameBuilder nameBuilder = new X500NameBuilder(BCStyle.INSTANCE);
                  nameBuilder.addRDN(BCStyle.CN, country);
                  nameBuilder.addRDN(BCStyle.O, company);
                  nameBuilder.addRDN(BCStyle.C, name);
                  X500Name x500Name = nameBuilder.build();

                  PKCS10CertificationRequestBuilder p10Builder = new JcaPKCS10CertificationRequestBuilder(
                          x500Name, keyPair.getPublic());
                  p10Builder.addAttribute(new ASN1ObjectIdentifier("1.2.840.10045.2.1"), new DERGeneralString("EC") );
                  
                  JcaContentSignerBuilder csBuilder = new JcaContentSignerBuilder(PkiConstant.ALGORITHM_ECC);
                  ContentSigner signer = csBuilder.build(keyPair.getPrivate());
                  csr = p10Builder.build(signer);

                  return csr;
              }

    /**
     * Create signer.
     * 
     * @param privateKey PrivateKey
     * @param algorithm SHA256withRSA/SHA256withECDSA/SM3withSM2
     * @return ContentSigner
     * @throws OperatorCreationException OperatorCreationException
     */
    public static ContentSigner getSigner(PrivateKey privateKey,
                                          String algorithm)
        throws OperatorCreationException {
        JcaContentSignerBuilder csBuilder = new JcaContentSignerBuilder(
                algorithm).setProvider(BouncyCastleProvider.PROVIDER_NAME);
        ContentSigner signer = csBuilder.build(privateKey);
        return signer;
    }

    /**
     * Verify cert.
     * 
     * @param certResult CertResult
     * @param localKey Public Key
     * @return cert chain
     * @throws Exception verify exception
     */
//    public static X509Certificate[] verify(CertResult1 certResult,
//                                           PublicKey localKey)
//        throws Exception {
//        // check the cert
//        byte[] byteEnd = Base64Utils.decode(certResult.getEndCert());
//        byte[] byteInter = Base64Utils.decode(certResult.getInterCert());
//        byte[] byteRoot = Base64Utils.decode(certResult.getRootCert());
//
//        try (InputStream inStream1 = new ByteArrayInputStream(byteEnd);
//                InputStream inStream2 = new ByteArrayInputStream(byteInter);
//                InputStream inStream3 = new ByteArrayInputStream(byteRoot)) {
//
//            X509Certificate endCert = (X509Certificate) CertificateFactory
//                    .getInstance(PkiConstant.X509,
//                            BouncyCastleProvider.PROVIDER_NAME)
//                    .generateCertificate(inStream1);
//            X509Certificate interCert = (X509Certificate) CertificateFactory
//                    .getInstance(PkiConstant.X509,
//                            BouncyCastleProvider.PROVIDER_NAME)
//                    .generateCertificate(inStream2);
//            X509Certificate rootCert = (X509Certificate) CertificateFactory
//                    .getInstance(PkiConstant.X509,
//                            BouncyCastleProvider.PROVIDER_NAME)
//                    .generateCertificate(inStream3);
//
//            endCert.verify(interCert.getPublicKey()); // 检查 终端证书是否由中级CA签发。
//            interCert.verify(rootCert.getPublicKey()); // 检查 中级证书是否由根级CA签发。
//                                                       // 根级证书也可以直接从LDAP获取。
//            if (!localKey.equals(endCert.getPublicKey())) {
//                throw new CertificateException("Public key not match.");
//            }
//            return new X509Certificate[] { endCert, interCert, rootCert };
//        }
//
//    }

    /**
     * Save cert to key store.
     * 
     * @param param KeyStoreParam
     * @param privateKey private key
     * @param keyStoreType key store type: JCELS, PKCS12
     * @throws Exception exception
     */
    public static void saveToKeystore(KeyStoreParam param,
                                      PrivateKey privateKey,
                                      String keyStoreType)
        throws Exception {

        KeyStore store = null;
        if (PkiConstant.PKCS12.equals(keyStoreType)) {
            store = KeyStore.getInstance(PkiConstant.PKCS12,
                    BouncyCastleProvider.PROVIDER_NAME);
        } else {
            store = KeyStore.getInstance(PkiConstant.JCEKS);
        }
        store.load(null, null);

        X509Certificate[] chain = param.getChain();

        store.setCertificateEntry(param.getCertAlias(), chain[0]); // 保存证书
        if (chain.length > 1 &&  null != param.getRootAlias()) {
            //保存根证书
            store.setCertificateEntry(param.getRootAlias(), chain[1]);
        }
        store.setKeyEntry(param.getPrivateKeyAlias(), privateKey,
                param.getPrivateKeyPassword().toCharArray(), chain); // 保存私钥

        File storeDir = new File(param.getPath());
        if (!storeDir.exists()) {
            storeDir.mkdirs();
        }

        File storeFile = new File(param.getKsFilePath());
        if (!storeFile.exists()) {
            storeFile.createNewFile();

            //不存在则创建
            try (FileOutputStream fos = new FileOutputStream(storeFile)) {

                store.store(fos, param.getKsPassword().toCharArray()); // Keystore
            }
            
            //保证证书到文件中，以cer格式
            if(chain.length == 1) {
                //根证书
                saveCertToFile(param.getPath()+"rootCert.cer",chain[0]);
            }
            if(chain.length > 1) {
                //OU证书
                saveCertToFile(param.getPath()+"ouCert.cer",chain[0]);
                
                //根证书
                saveCertToFile(param.getPath()+"rootCert.cer",chain[1]);
            }
            
        }else {
        	// key store文件已经存在，什么都不干!
        	
        }
        

    }

    /**
     * Get enroll keystore.
     * 
     * @param filePath keystore path + keystore file name
     * @param ksPassword keystore password
     * @param keyStoreType key store type: JCELS, PKCS12
     * @return keystore
     * @throws Exception exception
     */
    public static KeyStore getKeyStore(String filePath, String ksPassword,
                                       String keyStoreType)
        throws Exception {

        KeyStore store = null;
        File storeFile = new File(filePath);
        if (!storeFile.exists()) {
            return null;
        }
        try (FileInputStream fileInputStream = new FileInputStream(filePath)) {
            if (PkiConstant.JCEKS.equals(keyStoreType)) {
                store = KeyStore.getInstance(keyStoreType);
            } else {
                store = KeyStore.getInstance(keyStoreType,
                        BouncyCastleProvider.PROVIDER_NAME);
            }
            store.load(fileInputStream, ksPassword.toCharArray());
            return store;
        }

    }
    
    public static void saveCertToFile(String fileName,X509Certificate cert) {
        
        try {
            File file = new File(fileName);
            FileOutputStream fos = new FileOutputStream(file);
            
            fos.write(cert.getEncoded());
            fos.flush();
            fos.close();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        
    }
    
    public static X509Certificate  getCertFromCer(String filePath) {
        X509Certificate cert = null;
        byte[] certByte =  IOUtils.getBytes(filePath);
        ByteArrayInputStream inStream = new ByteArrayInputStream(certByte);
        
        try {
            cert = (X509Certificate) CertificateFactory
                    .getInstance(PkiConstant.X509,
                            BouncyCastleProvider.PROVIDER_NAME)
                    .generateCertificate(inStream);
            
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return cert;
       
    }
}
