/*
 * Copyright (c) 2017 Beijing Tiande Technology Co., Ltd.
 * All Rights Reserved.
 */
package cn.tdchain.jbcc.rpc;

import java.util.HashMap;

/**
 * 黑名单
 * @author xiaoming
 * 2019年4月18日
 */
public class BlackList {
	private static HashMap<String, Integer> blackList = new HashMap<String, Integer>();
	
	public static void addBlackListByHost(String host) {
		Integer v = blackList.get(host);
		if(v == null) {
			blackList.put(host, 1);
		}else {
			v = v + 1;
			blackList.put(host, v);
		}
	}
	
	public static boolean isBlackListByHost(String host) {
		Integer v = blackList.get(host);
		if(v != null && v > 100) {
			return true;
		}else {
			return false;
		}
	}
}
