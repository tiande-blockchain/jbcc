/*
 * Copyright (c) 2017 Beijing Tiande Technology Co., Ltd.
 * All Rights Reserved.
 */
package cn.tdchain.cipher.utils;

import cn.tdchain.jbcc.SQLCheckUtil;
/**
 * @Description: hash字符串验证，是否合法。
 * @author xiaoming
 * @date:下午2:26:37
 */
public class HashCheckUtil {
	/**
	 * @Description: 是正常的hash字符串返回true
	 * @param hash
	 * @return
	 * @throws
	 */
	public static boolean hashCheck(String hash) {
		if(hash == null || SQLCheckUtil.checkSQLError(hash) || illegalCharacterCheck(hash)) {
			return false;
		}else {
			return true;
		}
	}
	
	/**
	 * @Description: 是否包含特殊字符
	 * @param data
	 * @return
	 * @throws
	 */
	public static boolean illegalCharacterCheck(String data) {
		if(data == null) {
			return false;
		}else {
			return data.matches(".*['||;||\\\\||\n||\r].*");
		}
	}
}
