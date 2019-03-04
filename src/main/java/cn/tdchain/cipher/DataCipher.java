/*
 * Copyright (c) 2017 Beijing Tiande Technology Co., Ltd.
 * All Rights Reserved.
 */
package cn.tdchain.cipher;

/**
 * 数字密码传输效率更好
 * @author xiaoming
 * @date: 2018年11月1日 下午5:12:16
 */
public class DataCipher {
	private String passwd;//单向加密算法的密码，会被rsa公钥加密后保存起来。
	private String data;//被单向加密算法加密后的密文。
	private String sign;//发送者对data明文的hash信息签名后的数据
	
	public DataCipher() {}
	
	/**
	 * 数字密钥：使用公钥对数据加密，在网络上传输保证安全。
	 * @param passwd
	 * @param data
	 * @param publicKey
	 */
	public DataCipher(String passwd, String data, String privateKey, String publicKey, Cipher cipher) {
		// 1. 提取data的hash
		String hash = cipher.hash(data);
		
		// 2. 对hash进行签名
		String sign = cipher.signByPrivateKey(hash, privateKey);
		this.sign = sign;
		
		// 3.对data进行加密
		this.data = cipher.encrypt(data, passwd);
		
		// 4. 对passwd密码加密
		this.passwd = cipher.encryptByPublicKey(passwd, publicKey);
	}
	
	/**
	 * @Title: getData   
	 * @Description: 使用私钥进行解密得出明文 
	 * @param: @param privateKey
	 * @param: @return      
	 * @return: String      
	 * @throws
	 */
	public String getData(String privateKey, Cipher cipher) {
		String p = cipher.decryptByPrivateKey(this.passwd, privateKey);
		if(p == null) {
			return null;//解密失败
		}
		String data = cipher.decrypt(this.data, p);
		this.data = data;
		return data;//把解密后明文返回出去
	}
	
	/**
	 * @Description: 对信息进行验签
	 * @param senderPublicKey
	 * @param cipher
	 * @return
	 * @throws
	 */
	public boolean verify(String senderPublicKey, Cipher cipher) {
		String hash = cipher.hash(this.data);
		return cipher.verifyByPublicKey(hash, this.sign, senderPublicKey);
	}

	public String getPasswd() {
		return passwd;
	}

	public void setPasswd(String passwd) {
		this.passwd = passwd;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	public String getSign() {
		return sign;
	}

	public void setSign(String sign) {
		this.sign = sign;
	}
	
}
