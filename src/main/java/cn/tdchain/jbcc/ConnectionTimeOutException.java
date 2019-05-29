/*
 * Copyright (c) 2017 Beijing Tiande Technology Co., Ltd.
 * All Rights Reserved.
 */
package cn.tdchain.jbcc;
/**
 * jbcc connection连接超时时抛出此异常
 * @author xiaoming
 * 2019年3月28日
 */
public class ConnectionTimeOutException extends JbccTimeOutException {

	public ConnectionTimeOutException(String msg) {
		super(msg);
	}

}
