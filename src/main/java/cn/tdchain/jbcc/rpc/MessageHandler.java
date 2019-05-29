/*
 * Copyright (c) 2017 Beijing Tiande Technology Co., Ltd.
 * All Rights Reserved.
 */
package cn.tdchain.jbcc.rpc;

import cn.tdchain.jbcc.rpc.RPCMessage;

public interface MessageHandler {

    public String getResultMapByConnectionId(String connectionId, String sendPublicKey);

    public String handler(RPCMessage msg, String connectionId);
}
