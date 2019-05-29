/*
 * Copyright (c) 2017 Beijing Tiande Technology Co., Ltd.
 * All Rights Reserved.
 */
package cn.tdchain.jbcc.rpc;

import com.alibaba.fastjson.JSON;

import java.util.HashMap;
import java.util.Map;

/**
 * @author xiaoming
 * 2019年4月18日
 */
public class RPCMessage {
    private Long height;
    private Integer round;
    private TargetType targetType;
    private String sender;//发送者
    private String messageId;//请求消息对应的id，此id表示此返回信息是对于哪个消息返回的。
    private String target;//目标机器
    private String msg = "";
    private Map<String, String> command = new HashMap<String, String>();

    public TargetType getTargetType() {
        return targetType;
    }

    public void setTargetType(TargetType targetType) {
        this.targetType = targetType;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Long getHeight() {
        return height;
    }

    public void setHeight(Long height) {
        this.height = height;
    }

    public Map<String, String> getCommand() {
        return command;
    }

    public void setCommand(Map<String, String> command) {
        this.command = command;
    }

    public void addCommand(String key, String value) {
        this.command.put(key, value);
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String toJsonString() {
        return JSON.toJSONString(this);
    }

    public Integer getRound() {
        return round;
    }

    public void setRound(Integer round) {
        this.round = round;
    }

    public RPCMessage clone() {
        RPCMessage msg = new RPCMessage();
        msg.setCommand(this.getCommand());
        msg.setHeight(this.getHeight());
        msg.setRound(this.getRound());
        msg.setMsg(this.getMsg());
        msg.setSender(this.getSender());
        msg.setMessageId(this.getMessageId());
        msg.setTarget(this.getTarget());
        msg.setTargetType(this.getTargetType());
        return msg;
    }

    /**
     * broadcast target type
     *
     * @author xiaoming
     * 2019年4月18日
     */
    public enum TargetType {
        BATCH_REQUEST,
        BATCH_RESPHONE,
        TX_WAIT, HASHLIST, BLOCK, VOTE_1, VOTE_2,
        GET_PUBLICKEY, GET_BLOCK, GET_TOTAL_TRANS_COUNT, GET_TRANS_HASH, GET_TRANS_KEY, GET_SYSTEM_TRANS_KEY, GET_TRANS_LIST, GET_TRANS_HISTORY, GET_ACCOUNT_KEY_TRANS_HISTORY, GET_CONNECTION_COUNT,
        GET_NODE_LIST, NODE, METRONOME, REQUEST_NODE, GET_TRANS_LIST_BY_TYPE, GET_CURRENT_HEIGHT, GET_LEADER,
        CREATE_TEMPLATE, FREEZE_ACCOUNT, UNFREEZE_ACCOUNT, UPDATE_INFO, UPDATE_AUTH,
        CREATE_CONTRACT, RUN_CONTRACT, FREEZE_CONTRACT, UNFREEZE_CONTRACT, UPDATE_CONTRACT, MIGRATE_CONTRACT, GET_BLOCK_HEADER, GET_NEW_TRANS_LIST,

        // --------------------------------------------REPUTATION_REQ-----------------------------------------
        REPUTATION, REPUTATION_REQ, REPUTATION_VOTE
    }

}
