package cn.tdchain.cipher;
/**
 * @Description: 封装公私钥
 * @author xiaoming
 * @date:下午4:49:16
 */
public class Key {
	private String privateKey;
	private String publicKey;
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
}
