/*
 * Copyright (c) 2017 Beijing Tiande Technology Co., Ltd.
 * All Rights Reserved.
 */
package cn.tdchain.jbcc;
/**
 * @author xiaoming
 * 2019年4月18日
 */
public class PBFT {
	/**
	 * Description: 根据count获取拜占庭最小数
	 * @param count
	 * @return
	 */
	public static int getMinByCount(int count) {
		int a = (count * 2) / 3;
		int b = (count * 2) % 3;
		if(b > 0) {
			a = a + 1;
		}
		return a;
	}
}
