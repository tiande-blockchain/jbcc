/*
 * Copyright (c) 2017-2018 Beijing Tiande Technology Co., Ltd.
 * All Rights Reserved.
 */
package cn.tdchain.cipher.utils;

public class CipherUtil {
	public static String zeroSuffix(String passwd) {
		if (passwd.length() == 16) {
			return passwd;
		} else if (passwd.length() < 16) {
			passwd = passwd + "0000000000000000";
			return passwd.substring(0, 16);
		}
		return passwd.substring(0, 16);
	}
}
