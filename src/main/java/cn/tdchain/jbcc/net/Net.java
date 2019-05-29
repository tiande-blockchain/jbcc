/*
 * Copyright (c) 2017 Beijing Tiande Technology Co., Ltd.
 * All Rights Reserved.
 */
package cn.tdchain.jbcc.net;

import cn.tdchain.jbcc.net.info.Node;
import cn.tdchain.jbcc.rpc.RPCBatchResult;
import cn.tdchain.jbcc.rpc.RPCMessage;
import cn.tdchain.jbcc.rpc.RPCResult;
import com.alibaba.fastjson.TypeReference;

import java.util.List;

public interface Net {
	String heart = "heart";
	/**
	 * Description: 开启网络
	 */
	public void start();

	/**
	 * Description: net网络关闭
	 */
	public void stop();

	/**
	 * Description: 异步请求
	 * @param msg
	 */
	public void request(RPCMessage msg);

	/**
	 * Description: 异步响应，可设置超时时间。
	 * @param messageId
	 * @param timeOut
	 * @return List<RPCResult>
	 */
    RPCBatchResult resphone(String messageId, long timeOut);

	/**
	 * Description: 获取当前net网络中task任务数
	 * @return int
	 */
	public int getTaskSize();

	/**
	 * Description: 获取当前net网络中最小的在线节点数
	 * @return int
	 */
	public int getMinNodeSize();

	/**
	 * Description: 向net中添加新的Node对象。
	 * @param node
	 */
	public void addNodeToNodes(Node node);

	/**
	 * Description: 获取当前全部节点的node状态
	 * @return List<Node>
	 */
	public List<Node> getNodes();
}
