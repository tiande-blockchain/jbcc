/*
 * Copyright (c) 2017 Beijing Tiande Technology Co., Ltd.
 * All Rights Reserved.
 */
package cn.tdchain.jbcc.rpc.exception;

/**
 * TDRpcException.
 * 
 * @version 2.0
 * @author Xiaoming 2017-12-07
 */
public class RpcException extends RuntimeException {

    private static final long serialVersionUID = -8789880835205816495L;

    /**
     * RPC excepstion.
     * 
     * @param msg String
     */
    public RpcException(String msg) {
        super(msg);
    }
}
