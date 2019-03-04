/*
 * Copyright (c) 2017-2018 Beijing Tiande Technology Co., Ltd.
 * All Rights Reserved.
 */
package cn.tdchain.tdmsp.ca.root;

import java.io.ByteArrayInputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.cert.CertificateFactory;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.util.Date;

import javax.security.auth.x500.X500Principal;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.cert.X509CRLHolder;
import org.bouncycastle.cert.X509v2CRLBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CRLConverter;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.stereotype.Service;


import cn.tdchain.cipher.utils.StringUtils;
import cn.tdchain.tdmsp.ca.config.KeyStoreParam;
import cn.tdchain.tdmsp.ca.config.SystemConfig;
import cn.tdchain.tdmsp.util.PkiConstant;
import cn.tdchain.tdmsp.util.PkiUtil;


/**
 * RootCertificate.
 *
 * @version 2.0
 * @author Lijiating 2018-02-28
 */
public class RsaCertificate {

    static {
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }
//    protected static final  Logger log = LoggerFactory.getLogger(RsaCertificate.class);
    protected static SystemConfig systemConfig =  SystemConfig.getInstance();


    public void creatRootCA() {
//        log.info("Create root CA by RSA.");
        createRoot();
    }
    
    public void creatOuCert(KeyStoreParam keyStoreParam) {
//        log.info("Create OuCert  by RSA.");
        creatOrganizationCert(keyStoreParam);
    }
    
    public void creatOuCert(KeyStoreParam keyStoreParam,KeyStore rootkeyStore) {
//        log.info("Create OuCert  by RSA.");
        creatOrganizationCert(keyStoreParam,rootkeyStore);
    }

    
    protected void createRoot() {
        
        checkConfig(systemConfig);
        
        String[] issuer = systemConfig.getIssuerdn().split("@");
        X500Name x500Name = PkiUtil.getNameBuilder(issuer[0], issuer[1],
                issuer[2]);

        try {
                KeyPair keyPair = generateKeyPair();
                PublicKey pulickey = keyPair.getPublic();
                PrivateKey privateKey = keyPair.getPrivate();
    
                SubjectPublicKeyInfo subPubKeyInfo = SubjectPublicKeyInfo
                        .getInstance(pulickey.getEncoded());
    
                byte[] certBuf = PkiUtil.getRootEncodedHolder(subPubKeyInfo, x500Name,
                        Integer.valueOf(systemConfig.getValidTime()), x500Name,
                        privateKey, getAlgorithm());
                ByteArrayInputStream inStream = new ByteArrayInputStream(certBuf);
                       
                X509Certificate rootCA = (X509Certificate) CertificateFactory
                            .getInstance(PkiConstant.X509,
                                    BouncyCastleProvider.PROVIDER_NAME)
                            .generateCertificate(inStream);
    
                KeyStoreParam param = new KeyStoreParam(systemConfig.getAlias(),
                            null, systemConfig.getPrivateKeyAlias(),
                            systemConfig.getPrivateKeyPassword(),
                            systemConfig.getKsPassword(), systemConfig.getKsPath(),
                            systemConfig.getKsFileName(),
                            new X509Certificate[] { rootCA }, null,0);
                PkiUtil.saveToKeystore(param, privateKey, PkiConstant.PKCS12);
            
        } catch (Exception e) {
//            log.error(e.getMessage(), e);
        }
    }
    
    protected void creatOrganizationCert(KeyStoreParam keyStoreParam) {
        
        checkConfig(systemConfig);
        checkKeyStoreParam(keyStoreParam);
        
        try {
            KeyPair keyPair = generateKeyPair();

         // 获取根证书的keystore,以取根证书的公钥,dn,私钥
            KeyStore rootCaStore = getRootCaStore();
            
            X509Certificate rootCert = (X509Certificate) rootCaStore
                    .getCertificate(systemConfig.getAlias());
            X500Principal x500Principal = rootCert.getIssuerX500Principal();
            X500Name issuerDn = new X500Name(x500Principal.getName());

            PrivateKey rootPrivateKey = (PrivateKey) rootCaStore.getKey(
                    systemConfig.getPrivateKeyAlias(),
                    systemConfig.getPrivateKeyPassword().toCharArray());
            
            SubjectPublicKeyInfo subPubKeyInfo = SubjectPublicKeyInfo
                    .getInstance(keyPair.getPublic().getEncoded());
            
            X500Name x500Name = PkiUtil.getOuNameBuilder(keyStoreParam.getCommonName(), keyStoreParam.getOrganizationUnit(),
                    keyStoreParam.getCountry());


            byte[] certBuf = PkiUtil.getEncodedHolder(subPubKeyInfo, issuerDn,
                    keyStoreParam.getValidTime(),
                    x500Name, rootPrivateKey, getAlgorithm(),rootCert.getPublicKey());
           ByteArrayInputStream inStream = new ByteArrayInputStream(certBuf);
                  
           X509Certificate organizationCert = (X509Certificate) CertificateFactory
                        .getInstance(PkiConstant.X509,BouncyCastleProvider.PROVIDER_NAME)
                        .generateCertificate(inStream);          

           X509Certificate[] chain ={organizationCert,rootCert};
           keyStoreParam.setChain(chain);
           
           
           PkiUtil.saveToKeystore(keyStoreParam, keyPair.getPrivate(),
                    PkiConstant.PKCS12);
        } catch (Exception e) {
//            log.error(e.getMessage(), e);
        	e.printStackTrace();
        }

    }

    protected void creatOrganizationCert(KeyStoreParam keyStoreParam,KeyStore rootkeyStore) {
        try {
            KeyPair keyPair = generateKeyPair();

            X509Certificate rootCert = (X509Certificate) rootkeyStore
                    .getCertificate(systemConfig.getAlias());
            X500Principal x500Principal = rootCert.getIssuerX500Principal();
            X500Name issuerDn = new X500Name(x500Principal.getName());

            PrivateKey rootPrivateKey = (PrivateKey) rootkeyStore.getKey(
                    systemConfig.getPrivateKeyAlias(),
                    systemConfig.getPrivateKeyPassword().toCharArray());
            
            SubjectPublicKeyInfo subPubKeyInfo = SubjectPublicKeyInfo
                    .getInstance(keyPair.getPublic().getEncoded());
            
            X500Name x500Name = PkiUtil.getOuNameBuilder(keyStoreParam.getCommonName(), keyStoreParam.getOrganizationUnit(),
                    keyStoreParam.getCountry());


            byte[] certBuf = PkiUtil.getEncodedHolder(subPubKeyInfo, issuerDn,
                    keyStoreParam.getValidTime(),
                    x500Name, rootPrivateKey, getAlgorithm(),rootCert.getPublicKey());
           ByteArrayInputStream inStream = new ByteArrayInputStream(certBuf);
                  
           X509Certificate organizationCert = (X509Certificate) CertificateFactory
                        .getInstance(PkiConstant.X509,BouncyCastleProvider.PROVIDER_NAME)
                        .generateCertificate(inStream);          

           X509Certificate[] chain ={organizationCert,rootCert};
           keyStoreParam.setChain(chain);
           
           
           PkiUtil.saveToKeystore(keyStoreParam, keyPair.getPrivate(),
                    PkiConstant.PKCS12);
        } catch (Exception e) {
//            log.error(e.getMessage(), e);
        }

    }

    protected String getAlgorithm() {
        return PkiConstant.ALGORITHM_RSA;
    }

   
    protected KeyPair generateKeyPair() throws Exception {
        return PkiUtil.generateRsaKeyPair();
    }
   
    private KeyStore getRootCaStore() {
        try {
            return PkiUtil.getKeyStore(
                    systemConfig.getKsPath() + systemConfig.getKsFileName(),
                    systemConfig.getKsPassword(), PkiConstant.PKCS12);
        } catch (Exception e) {
//            log.error(e.getMessage(), e);
            return null;
        }
    }
    
    public void certRevocation(X509Certificate endCert, int reason)
       throws Exception {
          // 获取注册证书CA的keystore,以取注册证书的公钥,dn,私钥
          KeyStore enrollCaStore = getRootCaStore();
          
        
          X509Certificate rootCert = (X509Certificate) enrollCaStore
                  .getCertificate(systemConfig.getAlias());
          // 获取注册CA的私钥，用来进行签名
          PrivateKey privateKey = (PrivateKey) enrollCaStore.getKey(
                  systemConfig.getPrivateKeyAlias(),
                  systemConfig.getPrivateKeyPassword().toCharArray());
        
          X509v2CRLBuilder builder = new X509v2CRLBuilder(
                  new X500Name(rootCert.getSubjectDN().getName()), new Date());
        
          // 检查之前是否有作废列表，如果有，在其之上进行更新，如果没有，新生成crl列表。
//          TdX509CrlEntry tdX509CrlEntry = caRepo.getX509crl("");
//          if (null != tdX509CrlEntry) {
//              byte[] byteCrl = tdX509CrlEntry.getCertificateRevocationList();
//              X509CRLHolder x509crlHolder = new X509CRLHolder(byteCrl);
//              builder.addCRL(x509crlHolder);
//        
//          }
         
          builder.addCRLEntry(endCert.getSerialNumber(), new Date(), reason);
        
          X509CRLHolder crlHolder = builder
                  .build(PkiUtil.getSigner(privateKey, getAlgorithm()));
          JcaX509CRLConverter converter = new JcaX509CRLConverter()
                  .setProvider(BouncyCastleProvider.PROVIDER_NAME);
        
          X509CRL x509crl = converter.getCRL(crlHolder);
        
          
    }
    
    private void checkConfig(SystemConfig systemConfig) {
        
//        log.debug("checkConfig {}",JSON.toJSONString(systemConfig));
        
        if( StringUtils.isBlank(systemConfig.getIssuerdn())) {
            throw new RuntimeException("Please configure issuerdn in the application.properties");
        }
        if( StringUtils.isBlank(systemConfig.getAlias())) { 
            throw new RuntimeException("Please configure alias in the application.properties");
        }
        if( StringUtils.isBlank(systemConfig.getPrivateKeyAlias())) { 
            throw new RuntimeException("Please configure privateKey alias in the application.properties");
        }
        if( StringUtils.isBlank(systemConfig.getPrivateKeyPassword())) { 
            throw new RuntimeException("Please configure privateKey password in the application.properties");
        }
        if( StringUtils.isBlank(systemConfig.getKsPassword())) { 
            throw new RuntimeException("Please configure keyStore password in the application.properties");
        }
        if( StringUtils.isBlank(systemConfig.getKsPath())) { 
            throw new RuntimeException("Please configure keyStore path in the application.properties");
        }
        if( StringUtils.isBlank(systemConfig.getKsFileName())) { 
            throw new RuntimeException("Please configure keyStore fileName in the application.properties");
        }
        if( StringUtils.isBlank(systemConfig.getValidTime())) { 
            throw new RuntimeException("Please configure validTime in the application.properties");
        }
        
    }
    
    private void checkKeyStoreParam(KeyStoreParam keyStoreParam) {
//        log.debug("checkKeyStoreParam {}",JSON.toJSONString(keyStoreParam));
        
        if( StringUtils.isBlank(keyStoreParam.getCommonName())) {
            throw new RuntimeException("Please set commonName in the keyStoreParam");
        }
        if( StringUtils.isBlank(keyStoreParam.getOrganizationUnit())) {
            throw new RuntimeException("Please set organizationUnit in the keyStoreParam");
        }
        if( StringUtils.isBlank(keyStoreParam.getCountry())) {
            throw new RuntimeException("Please set country in the keyStoreParam");
        }
        if( StringUtils.isBlank(keyStoreParam.getCertAlias())) {
            throw new RuntimeException("Please set certAlias in the keyStoreParam");
        }
        if( StringUtils.isBlank(keyStoreParam.getRootAlias())) {
            throw new RuntimeException("Please set rootAlias in the keyStoreParam");
        }
        if( StringUtils.isBlank(keyStoreParam.getPrivateKeyAlias())) {
            throw new RuntimeException("Please set privateKeyAlias in the keyStoreParam");
        }
        if( StringUtils.isBlank(keyStoreParam.getPrivateKeyPassword())) {
            throw new RuntimeException("Please set privateKey password in the keyStoreParam");
        }
        if( StringUtils.isBlank(keyStoreParam.getPath())) {
            throw new RuntimeException("Please set keyStore path in the keyStoreParam");
        }
        if( StringUtils.isBlank(keyStoreParam.getKsPassword())) {
            throw new RuntimeException("Please set keyStore password in the keyStoreParam");
        }
        if( keyStoreParam.getValidTime() <= 0 ) {
            throw new RuntimeException("Please set cert validTime in the keyStoreParam");
        }
        
    }

    public static void main(String[] args) throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException {
        new RsaCertificate();
        
       
    }
    


}
