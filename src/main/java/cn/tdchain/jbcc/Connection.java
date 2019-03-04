
/*
 * Copyright (c) 2017 Beijing Tiande Technology Co., Ltd.
 * All Rights Reserved.
 */
package cn.tdchain.jbcc;

import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;

import cn.tdchain.Block;
import cn.tdchain.Trans;
import cn.tdchain.cipher.Cipher;
import cn.tdchain.cipher.CipherException;
import cn.tdchain.cipher.Key;
import cn.tdchain.cipher.utils.HashCheckUtil;
import cn.tdchain.jbcc.net.Net;
import cn.tdchain.jbcc.net.info.Node;
import cn.tdchain.jbcc.net.io.IONet;
import cn.tdchain.jbcc.net.nio.NioNet;
import cn.tdchain.jbcc.rpc.RPCMessage;
import cn.tdchain.jbcc.rpc.RPCResult;


/**
 * @Description: 天德区块链java版连接器，可以配置底层的连接池大小，一个connection对象可被多个线程并发使用。
 * @author xiaoming
 * @date: 2018年11月19日 下午5:21:24
 */
public class Connection {
	/**
	 * @Description: 保存着本连接器的公私钥
	 */
	private Key key = new Key();
	
	/**
	 * @Description: 密钥工具
	 */
	private Cipher cipher = null;
	
	/**
	 * @Description: 区块链节点的起始 ip 组，底层通过 see node 算法动态发现全部节点ip。
	 */
	private String[] ipTable = null;
	
	/**
	 * @Description: 区块链节点 RPC server 端口
	 */
	private int port = 18088;
	
	/**
	 * @Description: 客户端发起请求超时时间
	 */
	private long timeOut = 3000L;
	
	/**
	 * @Description: 连接器底层的长连接池大小。根据应用并发实际情况而定，一般默认为3。
	 */
	private int poolSize = 3;
	
	/**
	 * @Description: keystore文件路径
	 */
	private String ksPath = "";
	
	/**
	 * @Description: 打开keystore的密钥
	 */
	private String ksPasswd = "123456";
	
	/**
	 * @Description: 对口令长连接密码
	 */
	private String token = "1234567890123456";
	
	/**
	 * @Description: 每个连接器都又一个唯一标识
	 */
	private String connectionId = UUID.randomUUID().toString();//每个连接器都又一个唯一标识
	
	/**
	 * @Description: 底层实现异步提交请求、异步获取返回结果。
	 */
	protected Net net;
	
	/**
	 * @Description: 结果集吻合最小指数
	 */
	private int minSucces = 1;
	
	/**
	 * @param ipTable 区块链初始ip组
	 * @param port 区块链节点端口
	 * @param ksPath  keystore文件路径
	 * @param ksPasswd 打开keystore的密钥
	 * @param token 对口令长连接密码
	 * @param cipher 密钥工具对象
	 */
	public Connection(String[] ipTable, int port, String ksPath, String ksPasswd, String token, Cipher cipher
	                  ,String prikeyAlias,String prikePass,String pubKeyAlias) {
		this(ipTable, port, 3000, ksPath, ksPasswd, token, cipher, prikeyAlias, pubKeyAlias);
	}
	
	/**
	 * @param ipTable
	 * @param port
	 * @param key
	 * @param token
	 * @param cipher
	 */
	public Connection(String[] ipTable, int port, Key key, String token, Cipher cipher) {
		this.ipTable = ipTable;
		this.port = port;
		this.key = key;
		this.token = token;
		
		/**
		 * @Description: 根据初始化的iptable长度计算最小吻合指数。
		 */
		this.minSucces = PBFT.getMinByCount(ipTable.length);
		
		//异步定时同步在线nodes
		synAskNodes();
		
		//开启net网络
		startNet(token, cipher);
	}

	
	/**
	 * @param ipTable 区块链初始ip组
	 * @param port 区块链节点端口
	 * @param timeOut 设置网络超时时间
	 * @param ksPath keystore文件路径
	 * @param ksPasswd 打开keystore的密钥
	 * @param token 对口令长连接密码
	 * @param cipher 密钥工具对象
	 */
	public Connection(String[] ipTable, int port, long timeOut, String ksPath, String ksPasswd, String token, Cipher cipher
	                  , String prikeyAlias, String pubKeyAlias) {
		this.ipTable = ipTable;
		this.port = port;
		this.timeOut = timeOut;
		this.ksPath = ksPath;
		this.ksPasswd = ksPasswd;
		this.token = token;
		
		/**
		 * @Description: 根据初始化的iptable长度计算最小吻合指数。
		 */
		this.minSucces = PBFT.getMinByCount(ipTable.length);
		
		//初始化key、验证参数
		init(ksPath, ksPasswd, token, cipher, prikeyAlias, pubKeyAlias);
	}
	
	/**
	 * @Description: 新增交易
	 * @param Trans t
	 * @return Trans 返回当前交易对象，如果状态为succes说明交易已经成功落地。
	 * @throws
	 */
	public Trans addTrans(Trans t) {
		// 一条交易也被当做一个BatchTrans
		BatchTrans<Trans> batch = new BatchTrans<Trans>();
		batch.setConnectionId(this.connectionId);//标识交易来自哪个connection
		batch.addTransToBatch(t);
		
		batch = addBatchTrans(batch);
		return batch.oneTrans();
	}
	
	
	/**
	 * @Description: 批量提交交易集合
	 * @param batch
	 * @return
	 * @throws
	 */
	public BatchTrans<Trans> addBatchTrans(BatchTrans<Trans> batch) {
		batch.setConnectionId(this.connectionId);
		
		// 验证自己属性是否符合要求
		batch.check();

		RPCMessage msg = getMessage();
		msg.setMessageId(batch.getId());// 批处理的id作为本次消息唯一标识
		msg.setMsg(batch.toJsonString());
		msg.setTargetType(RPCMessage.TargetType.TX_WAIT);

		// 异步将交易发送给请求搬运工
		net.request(msg);

		// 异步等待交易返回
		List<RPCResult> r_list = net.resphone(msg.getMessageId(), 12000);// 12秒超时，如果共识超时也能知道交易是否成功

		/** 处理返回结果 */
		int succesCount = 0;
		int failCount = 0;
		BatchTrans<Trans> succes_batch = null;
		BatchTrans<Trans> fail_batch = null;
		for (RPCResult r : r_list) {
			BatchTrans<Trans> t_batch = null;
			if (r != null && r.getEntity() != null && r.getType() == RPCResult.ResultType.tx_status) {
				try {
					// 反序列化可能异常
					t_batch = JSON.parseObject(r.getEntity(), new TypeReference<BatchTrans<Trans>>() {
					});
				} catch (Exception e) {
					e.printStackTrace();
				}

				if (t_batch != null) {
					// hash验证
					succesCount++;// 成功一次
					succes_batch = t_batch;// 记住成功的交易对象
				} else {
					failCount++;// 失败一次
					fail_batch = t_batch;// 记住失败的交易对象
				}
			}
		}

		if (succesCount >= this.minSucces || succesCount > failCount) {
			return succes_batch;// 返回成功的交易对象
		} else {
			return fail_batch;
		}
	}
	
	/**
	 * @Description: 根据height查询整个block
	 * @param height
	 * @return Block
	 * @throws
	 */
    public Block<Trans> getBlock(Long height) {
    	Block<Trans> b = null;
    	// command
    	Map<String, String> command = new HashMap<String, String>();
    	command.put("height", height.toString());
    	//request
    	RPCMessage msg = getMessage();
		msg.setMessageId(UUID.randomUUID().toString());//交易hash标识唯一消息
		msg.setCommand(command);;
		msg.setTargetType(RPCMessage.TargetType.GET_BLOCK);
		
		net.request(msg);
		
		//resphonse
		List<RPCResult> r_list = net.resphone(msg.getMessageId(), 10000);
		for(RPCResult r:r_list) {
			if(r != null && (r.getType() == RPCResult.ResultType.block) && (r.getEntity() != null)) {
//				System.out.println("r.getEntity()=" + r.getEntity());
				try {
					b = JSON.parseObject(r.getEntity(), new TypeReference<Block<Trans>>(){});
				} catch (Exception e) {
					e.printStackTrace();
				}
				
		    	if(b != null) {
//		    		System.out.println(b.toJsonString());
		    		try {
		    			b.check();
		    			return b;//自身验证成功就返回
					} catch (BlockException e) {
						e.printStackTrace();
					}
		    	}
		    }
		}
		
		return null;
	}

	protected RPCMessage getMessage() {
		RPCMessage msg = new RPCMessage();
    	msg.setSender(this.connectionId);//标识该消息来自哪个connection
		return msg;
	}
    
    /**
     * @Description: 获取当前账本最高块信息
     * @return Block
     * @throws
     */
    public Block<Trans> getMaxBlock() {
    	return getBlock(-1L);
    }
	
    /**
     * @Description: 根据交易hash查询某条交易
     * @param hash
     * @return Trans 
     * @throws
     */
	public Trans getTransByHash(String hash) {
		boolean hashStatus = HashCheckUtil.hashCheck(hash);
		if(!hashStatus) {
			return null;
		}
		
    	// command
    	Map<String, String> command = new HashMap<String, String>();
    	command.put("hash", hash);
    	//request
    	RPCMessage msg = getMessage();
		msg.setMessageId(UUID.randomUUID().toString());//交易hash标识唯一消息
		msg.setCommand(command);
		msg.setTargetType(RPCMessage.TargetType.GET_TRANS_HASH);
		
		net.request(msg);
		
		//resphone
		List<RPCResult> r_list = net.resphone(msg.getMessageId(), 6000); //查询单个交易3秒超时
		return getTransByResult(r_list);
	}
	
	/**
	 * @Description: 根据交易hash集批量查询交易列表
	 * @param hashList
	 * @return List<Trans> 可能为空
	 * @throws
	 */
	public List<Trans> getTransListByHashList(List<String> hashList) {
		List<Trans> t_list = null;
		
		// command
    	Map<String, String> command = new HashMap<String, String>();
    	command.put("hashList", JSONObject.toJSONString(hashList));
    	//request
    	RPCMessage msg = getMessage();
    	msg.setMessageId(UUID.randomUUID().toString());//交易hash标识唯一消息
		msg.setCommand(command);
		msg.setTargetType(RPCMessage.TargetType.GET_TRANS_LIST);
		
		net.request(msg);
		
		//resphone
		List<RPCResult> r_list = net.resphone(msg.getMessageId(), 5000); //批量查询交易交易5秒超时
		t_list = getTransListByResultList(t_list, r_list);
		
		return t_list;
	}
	
	/**
	 * @Description: 根据维度 key 从账本中获取最新交易信息
	 * @param key
	 * @return Trans
	 * @throws
	 */
    public Trans getNewTransByKey(String key) {
    	key = key.trim();
    	boolean sqlStatus = SQLCheckUtil.checkSQLError(key);
    	if(sqlStatus) {
    		return null;//可能存在sql注入的key
    	}
    	
    	// command
    	Map<String, String> command = new HashMap<String, String>();
    	command.put("key", key);
    	//request
    	RPCMessage msg = getMessage();
    	msg.setMessageId(UUID.randomUUID().toString());//交易hash标识唯一消息
		msg.setCommand(command);
		msg.setTargetType(RPCMessage.TargetType.GET_TRANS_KEY);

		net.request(msg);
		
		//resphonse
		List<RPCResult> r_list = net.resphone(msg.getMessageId(), 3000); //根据key维度获取其最新交易3秒超时
		return getTransByResult(r_list);
	}
    
    public List<Trans> getTransListByType(String type){
    	//验证 type合法
    	if(HashCheckUtil.illegalCharacterCheck(type) || type == null || type.length() > 45) {
			throw new TransInfoException("type have Illegal character or length too long.");
		}
    	
        List<Trans> t_list = null;
		
		// command
    	Map<String, String> command = new HashMap<String, String>();
    	command.put("type", type);
    	//request
    	RPCMessage msg = getMessage();
    	msg.setMessageId(UUID.randomUUID().toString());//交易hash标识唯一消息
		msg.setCommand(command);
		msg.setTargetType(RPCMessage.TargetType.GET_TRANS_LIST_BY_TYPE);
		
		net.request(msg);
		
		//resphone
		List<RPCResult> r_list = net.resphone(msg.getMessageId(), 5000); //批量查询交易交易5秒超时
		t_list = getTransListByResultList(t_list, r_list);
		
		return t_list;
    }
    
    /**
     * @Description: 根据维度 key 从账本中获取其历史交易记录,最多一次查询30条,5秒时间超时。
     * @param key
     * @param startIndex 开始索引 0开始
     * @param endIdex 结束索引（包含此索引）
     * @return List<Trans>
     * @throws
     */
    public List<Trans> getTransHistoryByKey(String key, int startIndex, int endIndex) {
    	List<Trans> t_list = null;
    	
    	//验证参数
    	if(startIndex < 0 || startIndex > endIndex || ((endIndex - startIndex) > 30)) {
    		throw new ParameterException("startIndex < 0 || startIndex > endIndex || ((endIndex - startIndex) > 30)");
    	}
    	
    	boolean sqlStatus = SQLCheckUtil.checkSQLError(key);
    	if(sqlStatus) {
    		return t_list;//可能存在sql注入的key
    	}
    	
    	// command
    	Map<String, String> command = new HashMap<String, String>();
    	command.put("key", key);
    	command.put("startIndex", startIndex + "");
    	command.put("endIndex", endIndex + "");
    	
    	//request
    	RPCMessage msg = getMessage();
    	msg.setMessageId(UUID.randomUUID().toString());//交易hash标识唯一消息
		msg.setCommand(command);
		msg.setTargetType(RPCMessage.TargetType.GET_TRANS_HISTORY);
		
		net.request(msg);
		
		//resphonse
		List<RPCResult> r_list = net.resphone(msg.getMessageId(), 8000); //根据key 维度查询历史交易记录，超时8秒。
		t_list = getTransListByResultList(t_list, r_list);
    	
    	return t_list;
    }

	private List<Trans> getTransListByResultList(List<Trans> t_list, List<RPCResult> r_list) {
		for(RPCResult r:r_list) {
			if(r != null && r.getType() == RPCResult.ResultType.tx_list && r.getEntity() != null) {
				try {
					t_list = JSON.parseArray(r.getEntity(), Trans.class);
					if(t_list != null) {
						break;//找到立即退出
					}
				} catch (Exception e) {
				}
		    }
		}
		return t_list;
	}
    
    /**
     * @Description: 根据维度数组开启事务。如果涉及维度有其他线程已经开启事务的，当前线程阻塞等待，如果超过3秒后原事务仍未被释放的，当前线程强制开启事务。
     * @param keys
     * @return true/false 事务开启成功则返回true  失败则返回false
     * @throws
     */
    public boolean startTransaction(String[] keys) {
    	if(keys != null && keys.length > 0) {
    		//获取事务对象
        	Transaction t = new Transaction(keys);
        	//将事务提交到事务池，如果成功当前现在则锁住所有关于key的添加操作，在事务池中最多保留事务对象到stop time时间
        	return ManagerTransactionPool.register(t, 6000);
    	}else {
    		return false;
    	}
    }
    
    /**
     * @Description: 根据维度数组关闭事务。
     * @param keys
     * @throws
     */
    public void stopTransaction(String[] keys) {
    	if(keys != null && keys.length > 0) {
    		//获取事务对象
        	Transaction t = new Transaction(keys);
        	ManagerTransactionPool.destroy(t);
    	}
    }
    
    
    private Trans getTransByResult(List<RPCResult> r_list) {
    	int succesCount = 0;
		int failCount = 0;
		Trans succes_t = null;
		Trans fail_t = null;
		for(RPCResult r:r_list) {
			Trans r_t = null;
			if(r != null && r.getEntity() != null && r.getType() == RPCResult.ResultType.tx_status) {
				try {
					//反序列化可能异常
					r_t = JSONObject.parseObject(r.getEntity(), Trans.class);
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				if(r_t != null && TransUtil.getTransHash(r_t).equals(r_t.getHash())) {
					//hash验证
					succesCount++;//成功一次
					succes_t = r_t;//记住成功的交易对象
				}else {
					failCount++;//失败一次
					fail_t = r_t;//记住失败的交易对象
				}
			}
		}
		
		if(succesCount >= this.minSucces || succesCount > failCount) {
			return succes_t;//返回成功的交易对象
		}else {
			return fail_t;
		}
    }
    
    
    private void init(String ksPath, String ksPasswd, String token, Cipher cipher
                      ,String prikeyAlias, String pubKeyAlias) {
		try {
		 // 读取key store中的公私钥
          String privateKey = cipher.getPrivateKeyStringByKeyStore(ksPath, ksPasswd, prikeyAlias);
          String publicKey = cipher.getPublicKeyStringByStore(ksPath, ksPasswd, pubKeyAlias);
            
//            KeyStore keyStore =  cipher.getKeyStore(ksPath, ksPasswd);
//            PrivateKey privateKey = (PrivateKey) keyStore.getKey(prikeyAlias, prikePass.toCharArray());
//            X509Certificate  cert =  (X509Certificate) keyStore.getCertificate(pubKeyAlias);
//            
//            String privateKeystr = Base64Utils.encode(privateKey.getEncoded());
//            String publicKeystr = Base64Utils.encode(cert.getPublicKey().getEncoded());
            
            key.setPrivateKey(privateKey);
            key.setPublicKey(publicKey);
		} catch (Exception e) {
			throw new CipherException("get private key by key store error:" + e.getMessage());
		}
		
		//异步定时同步在线nodes
		synAskNodes();
		
		//开启net网络
		startNet(token, cipher);
	}
    
	private void startNet(String token, Cipher cipher) {
		// 验证token不能为空
		if(token == null || token.length() == 0) {
			throw new ParameterException("token is null");
		}
				
		// 开启io net网络
		net = new IONet(this.ipTable, this.port, cipher, token, this.key, this.connectionId);
//		net = new NioNet(this.ipTable, this.port, cipher, token, this.key, this.connectionId);
		net.start();
		
		
		while(true) {
			if(net.getTaskSize() >= net.getMinNodeSize()) {
				break;
			}else {
				try {
					Thread.sleep(20);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * @Description: 异步定时请求server获取全部在线节点
	 * @throws
	 */
	private void synAskNodes() {
		
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				while(true) {
					try {
						RPCMessage msg = getMessage();
						msg.setTargetType(RPCMessage.TargetType.GET_NODE_LIST);
						msg.setMessageId(UUID.randomUUID().toString());
						//异步提交请求
						net.request(msg);
						
						//resphonse
						List<Node> nodes = new ArrayList<Node>();
						List<RPCResult> r_list = net.resphone(msg.getMessageId(), timeOut);
						for(RPCResult r:r_list) {
							if(r != null && (r.getType() == RPCResult.ResultType.node_list) && (r.getEntity() != null)) {
								List<Node> t_nodes = JSONObject.parseArray(r.getEntity(), Node.class);
								if(t_nodes != null && t_nodes.size() > 0) {
									if(t_nodes.size() > nodes.size()) {
										nodes = t_nodes;
									}
								}
						    }
						}
						
						
						//获取在线的节点添加到net中
						for(int i = 0; nodes != null && i < nodes.size(); i++) {
							Node n = nodes.get(i);
							System.out.println("copy node id:" + n.getId()  + " status=" + n.getStatus());
//							if(n.getStatus() == Node.NodeStatus.METRONOMER) {
//								System.out.println("服务地址：" + n.getHost());
								net.addNodeToNodes(n);
//							}
						}
						
						
						//3秒循环一次
						Thread.sleep(3000);
					} catch (Exception e) {
						
					}
				}
			}
		}).start();
		
	}
	
	/**
	 * 获取当前系统全部节点的node对象集合
	 * @Description: 
	 * @return
	 * @throws
	 */
	public List<Node> getBlockChainNodeStatus() {
		List<Node> nodes = net.getNodes();
		return nodes;
	}
    
    protected String getConnectionId() {
    	return this.connectionId;
    }
	
}

