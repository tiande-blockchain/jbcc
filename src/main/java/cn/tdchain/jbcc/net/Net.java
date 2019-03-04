package cn.tdchain.jbcc.net;

import java.util.List;

import cn.tdchain.jbcc.net.info.Node;
import cn.tdchain.jbcc.rpc.RPCMessage;
import cn.tdchain.jbcc.rpc.RPCResult;

public interface Net {
	/**
	 * @Description: 开启网络
	 * @throws
	 */
	public void start();
	
	/**
	 * @Description: 异步请求
	 * @param msg
	 * @throws
	 */
	public void request(RPCMessage msg);
	
	/**
	 * @Description: 异步响应，可设置超时时间。
	 * @param messageId
	 * @param timeOut
	 * @return
	 * @throws
	 */
	public List<RPCResult> resphone(String messageId, long timeOut);
	
	/**
	 * @Description: 获取当前net网络中task任务数
	 * @return
	 * @throws
	 */
	public int getTaskSize();
	
	/**
	 * @Description: 获取当前net网络中最小的在线节点数
	 * @return
	 * @throws
	 */
	public int getMinNodeSize();
	
	/**
	 * @Description: 向net中添加新的Node对象。
	 * @param node
	 * @throws
	 */
	public void addNodeToNodes(Node node);

	/**
	 * @return 
	 * 获取当前全部节点的node状态
	 * @Description: 
	 * @throws
	 */
	public List<Node> getNodes();
}
