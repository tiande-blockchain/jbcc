/*
 * Copyright (c) 2017 Beijing Tiande Technology Co., Ltd.
 * All Rights Reserved.
 */
package cn.tdchain.jbcc;

/**
 * Description: SQL语句检查，是否有注入风险等，或者非法特殊字符。
 * @author xiaoming
 * 2019年4月18日
 */
public class SQLCheckUtil {
	/**
	 * Description: sql注入检查、特俗字符检查
	 * @param param
	 * @return boolean
	 */
	public static boolean checkSQLError(String param) {
		if(param.contains("=") || param.contains(" or ") || param.contains(" and ") || param.contains("'")) {
			return true;
		}else {
			return false;
		}
	}
}
