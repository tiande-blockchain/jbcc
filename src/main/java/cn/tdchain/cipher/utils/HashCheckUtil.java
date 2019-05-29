/*
 * Copyright (c) 2017 Beijing Tiande Technology Co., Ltd.
 * All Rights Reserved.
 */
package cn.tdchain.cipher.utils;

import cn.tdchain.jbcc.SQLCheckUtil;
/**
 * hash字符串验证，是否合法。
 * @author xiaoming
 * 2019年4月18日
 */
public class HashCheckUtil {
	/**
	 * hashCheck是正常的hash字符串返回true
	 * 
	 * @param hash
	 * @return
	 */
	public static boolean hashCheck(String hash) {
		if(hash == null || SQLCheckUtil.checkSQLError(hash) || illegalCharacterCheck(hash)) {
			return false;
		}else {
			return true;
		}
	}
	
	/**
	 * illegalCharacterCheck是否包含特殊字符
	 * @param data
	 * @return
	 */
	public static boolean illegalCharacterCheck(String data) {
		if(data == null) {
			return false;
		}else {
			return data.matches(".*['||;||\\\\||\n||\r].*");
		}
	}
}
