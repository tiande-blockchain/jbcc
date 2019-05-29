/*
 * Copyright (c) 2017 Beijing Tiande Technology Co., Ltd.
 * All Rights Reserved.
 */
package cn.tdchain.cipher;
/**
 * Description: 封装公私钥
 * @author xiaoming
 * 2019年4月18日
 */
public class Key {
	/**
	 * 本节点私钥字符串
	 */
	private String privateKey;
	
	/**
	 * 本节点公钥字符串
	 */
	private String publicKey;
	
	/**
	 * 本地证书的base64字符串
	 */
	private String localCertBase64String = "";
	
	/**
	 * root证书的base64字符串
	 */
	private String rootCertBase64String = "";
	
	
	public String getPrivateKey() {
		return privateKey;
	}
	public void setPrivateKey(String privateKey) {
		this.privateKey = privateKey;
	}
	public String getPublicKey() {
		return publicKey;
	}
	public void setPublicKey(String publicKey) {
		this.publicKey = publicKey;
	}
	
	public String getLocalCertBase64String() {
		return localCertBase64String;
	}
	public void setLocalCertBase64String(String localCertBase64String) {
		this.localCertBase64String = localCertBase64String;
	}
	public String getRootCertBase64String() {
		return rootCertBase64String;
	}
	public void setRootCertBase64String(String rootCertBase64String) {
		this.rootCertBase64String = rootCertBase64String;
	}
	
}
