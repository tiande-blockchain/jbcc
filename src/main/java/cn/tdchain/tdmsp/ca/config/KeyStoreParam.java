/*
 * Copyright (c) 2017-2018 Beijing Tiande Technology Co., Ltd.
 * All Rights Reserved.
 */
package cn.tdchain.tdmsp.ca.config;

import java.security.cert.X509Certificate;

/**
 * Keystore Parameters.
 *
 * @version 2.0
 * @author Houmj 2018-09-20
 */
public class KeyStoreParam {

    private String certAlias;

    private String rootAlias;

    private String privateKeyAlias;

    private String privateKeyPassword;

    private String ksPassword;

    private String path;

    private String fileName;

    private X509Certificate[] chain;

    private String cipherType;
    
    private String country;
    
    private String organizationUnit;
    
    private String commonName;
    
    private int validTime;

    /**
     * Default constructor.
     */
    public KeyStoreParam() {
    }

    /**
     * Constructor.
     * 
     * @param ecertAlias String
     * @param rootAlias String
     * @param keyAlias String
     * @param certPassword String
     * @param ksPassword String
     * @param path key store path
     * @param fileName key store file name
     * @param chain X509Certificate[]
     * @param cipherType cipher type
     */
    public KeyStoreParam(String certAlias, String rootAlias, String privateKeyAlias,
            String privateKeyPassword, String ksPassword, String path,
            String fileName, X509Certificate[] chain, String cipherType,int validTime) {
        this.certAlias = certAlias;
        this.rootAlias = rootAlias;
        this.privateKeyAlias = privateKeyAlias;
        this.privateKeyPassword = privateKeyPassword;
        this.ksPassword = ksPassword;
        this.path = path;
        this.fileName = fileName;
        this.chain = chain;
        this.cipherType = cipherType;
        this.validTime = validTime;
    }

   

    public int getValidTime() {
        return validTime;
    }

    public void setValidTime(int validTime) {
        this.validTime = validTime;
    }

    public String getCertAlias() {
        return certAlias;
    }

    public void setCertAlias(String certAlias) {
        this.certAlias = certAlias;
    }

    public String getRootAlias() {
        return rootAlias;
    }

    public void setRootAlias(String rootAlias) {
        this.rootAlias = rootAlias;
    }


    public String getPrivateKeyAlias() {
        return privateKeyAlias;
    }

    public void setPrivateKeyAlias(String privateKeyAlias) {
        this.privateKeyAlias = privateKeyAlias;
    }

    public String getPrivateKeyPassword() {
        return privateKeyPassword;
    }

    public void setPrivateKeyPassword(String privateKeyPassword) {
        this.privateKeyPassword = privateKeyPassword;
    }

    public String getKsPassword() {
        return ksPassword;
    }

    public void setKsPassword(String ksPassword) {
        this.ksPassword = ksPassword;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public X509Certificate[] getChain() {
        return chain;
    }

    public void setChain(X509Certificate[] chain) {
        this.chain = chain;
    }

    public String getCipherType() {
        return cipherType;
    }

    public void setCipherType(String cipherType) {
        this.cipherType = cipherType;
    }
    
    public String getCountry() {
        return country;
    }
    public void setCountry(String country) {
        this.country = country;
    }

    public String getOrganizationUnit() {
        return organizationUnit;
    }

    public void setOrganizationUnit(String organizationUnit) {
        this.organizationUnit = organizationUnit;
    }

    public String getCommonName() {
        return commonName;
    }

    public void setCommonName(String commonName) {
        this.commonName = commonName;
    }

    /**
     * Get key store absolute path with file name.
     * 
     * @return key store path and file name
     */
    public String getKsFilePath() {
        if (this.getCipherType() == null) {
            return this.getPath() + this.getFileName();
        }
        return this.getPath() + this.getCipherType() + "_" + this.getFileName();
    }

}
