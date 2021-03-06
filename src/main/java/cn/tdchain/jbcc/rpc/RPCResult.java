package cn.tdchain.jbcc.rpc;

import com.alibaba.fastjson.JSON;

/**
 * rpc 返回对象
 * connectionId 标识返回的目的地
 * messageId 标识着返回的阻塞线程
 * startTime 标识着该对象的生命起始时间，一旦超过周期范围则被系统丢弃。
 * 
 * @author xiaoming
 * @date: 2018年11月15日 下午4:34:41
 */
public class RPCResult {
	public static String PRC_RESULT_DESCRYPT_ERROR = "PRC-RESULT-DESCRYPT-ERROR"; //通知客户端服务端解密异常的消息
	
	private StatusType status = StatusType.fail;
	private ResultType type = null;
	private String entity = "";//json字符串
	private String target;//目标机器的ip可能是公网ip
	private String connectionId;// 发送者、或者是connection_id
	private String messageId;//请求消息对应的id，此id表示此返回信息是对于哪个消息返回的。
	private long startTime;//起始生命周期，如果超过生命周期系统会丢弃本对象。
	

	public StatusType getStatus() {
		return status;
	}

	public void setStatus(StatusType status) {
		this.status = status;
	}

	public ResultType getType() {
		return type;
	}

	public void setType(ResultType type) {
		this.type = type;
	}

	public String getEntity() {
		return entity;
	}

	public void setEntity(String entity) {
		this.entity = entity;
	}

	public String getTarget() {
		return target;
	}

	public void setTarget(String target) {
		this.target = target;
	}

	public String getConnectionId() {
		return connectionId;
	}

	public void setConnectionId(String connectionId) {
		this.connectionId = connectionId;
	}

	public String getMessageId() {
		return messageId;
	}

	public void setMessageId(String messageId) {
		this.messageId = messageId;
	}

	public long getStartTime() {
		return startTime;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	public enum ResultType {
		block, tx_status, tx_list, result_list, batch_resphone, msg_error, node_list,resphone_node, current_height, node_info
	}

	public enum StatusType {
		succes, fail, timeout
	}

	public String toJSONString() {
		return JSON.toJSONString(this);
	}

}
