//package cn.tdchain.cipher.cuda.client;
//
//import cn.tdchain.cipher.Cipher;
//import cn.tdchain.cipher.Key;
//
//public class Test {
//
//	public static void main(String[] args) {
//		Cipher c = new Cipher();
//		Key key = c.generateKey();
//		try {
////			CUDACipherClient cuda = new CUDACipherClient("192.168.0.9", 8080, 1);
////			System.out.println(cuda);
//			
//			
//			while(true) {
//				//公钥加密
//				String data = "李明顶";
//				String c_text = c.encryptByPublicKey(data, key.getPublicKey());
//				
//				//cuda私钥解密
//				String data_new = c.decryptByPrivateKey(c_text, key.getPrivateKey());
////				String data_new = cuda.rsa_decrypt(c_text, key.getPrivateKey());
//				System.out.println("解密明文:" + data_new);
//				
//				Thread.sleep(1000);
//			}
//			
//		} catch (Exception e) {
//			System.out.println("获取cuda连接客户端错误:" + e.getMessage());
//		}
//		
//		
//		
//	}
//
//}
