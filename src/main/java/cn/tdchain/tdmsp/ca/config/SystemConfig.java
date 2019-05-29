/*
 * Copyright (c) 2019 Beijing Tiande Technology Co., Ltd.
 * All Rights Reserved.
 */
package cn.tdchain.tdmsp.ca.config;

import java.io.File;
import java.security.cert.X509Certificate;

import cn.tdchain.cipher.utils.CipherUtil;
import cn.tdchain.tdmsp.Msp;

/**
 * Description:枚举单例
 * @version 1.0
 * @author jiating 2019-01-02
 */
public class SystemConfig {
	private String RSA_ISSUERDN = "rsarootca@tdbc@CN";
	private String SM_ISSUERDN = "smrootca@tdbc@CN";
	
	/**
	 * Description:
	 */
    private  String issuerdn;
    private  String rootAlias;
    private  String rootPassword;
    private  String rootKsPath;
    private  String rootKsFileName;
    private  int validTime;
    
    /**
     * Description:证书特有属性
     */
    private String organizationUnit;//组织机构
    private String country;//国家CN
    private String commonName;
    private X509Certificate[] chain;
    private String certAlias; //组织证书才会让用户自己设置别名
    private  String certPassword;
    private  String certKsPath;
    private  String certKsFileName;
    
    public SystemConfig(String rootKsPath, String rootPasswd, String organizationUnit, String commonName, String certAlias, String certKsPath, String certPassword) {
    	this(rootKsPath, rootPasswd);
    	this.organizationUnit = organizationUnit;
    	this.commonName = commonName;
    	this.country = "CN";
    	this.certAlias = certAlias;
    	
    	this.certKsPath = certKsPath.substring(0, certKsPath.lastIndexOf(File.separatorChar) + 1);
    	this.certKsFileName = certKsPath.substring(certKsPath.lastIndexOf(File.separatorChar) + 1, certKsPath.length());
    	certPassword = CipherUtil.zeroSuffix(certPassword);
    	this.certPassword = certPassword;
    }
    
    public SystemConfig(String rootKsPath, String rootPasswd) {
    	rootPasswd = CipherUtil.zeroSuffix(rootPasswd);
    	
    	this.issuerdn = initIssuerdn();
    	this.rootAlias = Msp.ROOT_ALIAS;
    	this.rootPassword = rootPasswd;
    	this.rootKsPath = rootKsPath.substring(0, rootKsPath.lastIndexOf(File.separatorChar) + 1);
    	this.rootKsFileName = rootKsPath.substring(rootKsPath.lastIndexOf(File.separatorChar) + 1, rootKsPath.length());
    	this.validTime = 36500; //这里是36500天
    	this.certAlias = Msp.ROOT_ALIAS;
    }
    

	private String initIssuerdn() {
		if(Msp.getType() == Msp.Type.RSA) {
			return this.RSA_ISSUERDN;
		}else {
			return this.SM_ISSUERDN;
		}
	}

	public String getIssuerdn() {
		return issuerdn;
	}

	public void setIssuerdn(String issuerdn) {
		this.issuerdn = issuerdn;
	}


	public int getValidTime() {
		return validTime;
	}

	public void setValidTime(int validTime) {
		this.validTime = validTime;
	}


	public String getOrganizationUnit() {
		return organizationUnit;
	}


	public void setOrganizationUnit(String organizationUnit) {
		this.organizationUnit = organizationUnit;
	}


	public String getCountry() {
		return country;
	}


	public void setCountry(String country) {
		this.country = country;
	}


	public String getCommonName() {
		return commonName;
	}


	public void setCommonName(String commonName) {
		this.commonName = commonName;
	}


	public X509Certificate[] getChain() {
		return chain;
	}


	public void setChain(X509Certificate[] chain) {
		this.chain = chain;
	}

	public String getCertAlias() {
		return certAlias;
	}

	public void setCertAlias(String certAlias) {
		this.certAlias = certAlias;
	}

	public String getRSA_ISSUERDN() {
		return RSA_ISSUERDN;
	}

	public void setRSA_ISSUERDN(String rSA_ISSUERDN) {
		RSA_ISSUERDN = rSA_ISSUERDN;
	}

	public String getSM_ISSUERDN() {
		return SM_ISSUERDN;
	}

	public void setSM_ISSUERDN(String sM_ISSUERDN) {
		SM_ISSUERDN = sM_ISSUERDN;
	}

	public String getRootAlias() {
		return rootAlias;
	}

	public void setRootAlias(String rootAlias) {
		this.rootAlias = rootAlias;
	}

	public String getRootPassword() {
		return rootPassword;
	}

	public void setRootPassword(String rootPassword) {
		this.rootPassword = rootPassword;
	}

	public String getRootKsPath() {
		return rootKsPath;
	}

	public void setRootKsPath(String rootKsPath) {
		this.rootKsPath = rootKsPath;
	}

	public String getRootKsFileName() {
		return rootKsFileName;
	}

	public void setRootKsFileName(String rootKsFileName) {
		this.rootKsFileName = rootKsFileName;
	}

	public String getCertPassword() {
		return certPassword;
	}

	public void setCertPassword(String certPassword) {
		this.certPassword = certPassword;
	}

	public String getCertKsPath() {
		return certKsPath;
	}

	public void setCertKsPath(String certKsPath) {
		this.certKsPath = certKsPath;
	}

	public String getCertKsFileName() {
		return certKsFileName;
	}

	public void setCertKsFileName(String certKsFileName) {
		this.certKsFileName = certKsFileName;
	}
    
}
