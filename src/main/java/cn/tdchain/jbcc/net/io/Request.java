package cn.tdchain.jbcc.net.io;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import cn.tdchain.cipher.Cipher;
import cn.tdchain.cipher.DataCipher;
import cn.tdchain.cipher.Key;
import cn.tdchain.jbcc.net.io.IONet.Task;
import cn.tdchain.jbcc.rpc.RPCMessage;
import cn.tdchain.jbcc.rpc.exception.RpcException;
import cn.tdchain.jbcc.rpc.io.client.RpcClient;

/**
 * @Description: 异步提交请求消息
 * @author xiaoming
 * @date:上午10:59:25
 */
public class Request{
//	private Executor executor = Executors.newFixedThreadPool(8000);
	
	private Task task;
	
	private int workerNum = 3;// 工人数，默认3名.
	
	private RequestPool pool;
	
	private String ip;
	
	private Cipher cipher;
	
	private String token;
	
	private Key key;
	
	private boolean status = true;// true=在线     false=死忙的
	
	private String connectionId;
	
	private String serverPublicKey = null;// 缓存server 的公钥
	

	public Request(Task task, String serverHost, int serverPort, Cipher cipher, String token, Key key, String connectionId, int workerNum, String serverPublicKey) {
		this.task = task;
		this.connectionId = connectionId;
		this.key = key;
		this.token = token;
		this.cipher = cipher;
		this.ip = serverHost;
		if(workerNum > this.workerNum) {
			this.workerNum = workerNum;
		}
		this.serverPublicKey = serverPublicKey;
		
		this.pool = new RequestPool();
	}

	int error_num = 0;
	public void start() {
		this.pool.start();
		
		if(this.status) {
			long wait_time = 50;
			// 开始异步批量提交请求
			for(int i = 0; i < this.workerNum; i++) {
//			for(int i = 0; i < 1; i++) {
				
				new Thread(new Runnable() {
					
					@Override
					public void run() {
						
//						/** 定时器版本 **/
//						ScheduledThreadPoolExecutor my_timer = new ScheduledThreadPoolExecutor(2);  
//						my_timer.scheduleAtFixedRate(new Runnable() {
//							
//							@Override
//							public void run() {
//								if(status) {
//								/** 继续搬运消息列表给server */
//								List<RPCMessage> msgList = pool.getMsgList(1000);
//								
//								if(msgList.size() > 0) {
////									System.out.println(Thread.currentThread().getName() + "   msgList.size=" + msgList.size());
//									//封装批量消息体
//									RPCMessage batch_msg = new RPCMessage();
//									batch_msg.setTarget(ip);
//									batch_msg.setTargetType(RPCMessage.TargetType.BATCH_REQUEST);
//									batch_msg.setSender(connectionId);//
//									batch_msg.setMsg(JSONObject.toJSONString(msgList));
//									
//									//发送如果出现网络异常超过三次则判断为该节点下线。结束本任务
//									AIORpcClient client = null;
//									try {
//										//获取连接对象
//										client = task.getClient();
//										
//										// 发送
//										client.send(batch_msg.toJsonString());
//										
//										
//										//重置error_num = 0
//										error_num = 0;
//									}catch (SocketTimeoutException e) {
//										e.printStackTrace();
//									}catch (ConnectException e) {
//										error_num = close(error_num, client, e);
//									} catch (IOException e) {
//										error_num = close(error_num, client, e);
//									} catch (RpcException e) {
//										error_num = close(error_num, client, e);
//									} catch (Exception e) {
//										error_num = close(error_num, client, e);
//									}finally {
//										//归还client
//										task.returnClient(client);
//									}
//									
//									if(error_num > 10) {
//										task.stop();//可能出现网络异常，需要结束整个task任务。
//									}
//								}
//							}else {
//								my_timer.shutdownNow();//结束搬运工作
//							}
//							}
//						}, 20, wait_time, TimeUnit.MILLISECONDS);
						
						
						
						/** sleep版本 */
						while(true) {
							
							if(status) {
								/** 继续搬运消息列表给server */
								List<RPCMessage> msgList = pool.getMsgList(1000);
								
								if(msgList.size() > 0) {
									
									//封装批量消息体
									RPCMessage batch_msg = new RPCMessage();
									batch_msg.setTarget(ip);
									batch_msg.setTargetType(RPCMessage.TargetType.BATCH_REQUEST);
									batch_msg.setSender(connectionId);//
									batch_msg.setMsg(JSONObject.toJSONString(msgList));
									
									/** start 数字信封发送 */
									DataCipher data = new DataCipher(UUID.randomUUID().toString(), batch_msg.getMsg(),  key.getPrivateKey(),
											serverPublicKey, cipher);
									batch_msg.setMsg(JSON.toJSONString(data));// 更新密文发送
									/** end 数字信封发送 */
									
									
									//发送如果出现网络异常超过三次则判断为该节点下线。结束本任务
									RpcClient client = null;
									try {
										//获取连接对象
										client = task.getClient();
										
										// 发送
										client.send(batch_msg.toJsonString());
										
										
										//重置error_num = 0
										error_num = 0;
									}catch (SocketTimeoutException e) {
										e.printStackTrace();
									}catch (ConnectException e) {
										error_num = close(error_num, client, e);
									} catch (IOException e) {
										error_num = close(error_num, client, e);
									} catch (RpcException e) {
										error_num = close(error_num, client, e);
									} catch (Exception e) {
										error_num = close(error_num, client, e);
									}finally {
										//归还client
										task.returnClient(client);
									}
									
									if(error_num > 5) {
										task.stop();//可能出现网络异常，需要结束整个task任务。
									}
								}
							}else {
								break;//结束搬运工作
							}
							
							//间接休息
							try {
								Thread.sleep(wait_time);
							} catch (InterruptedException e) {
							}
						}
					}

					private int close(int error_num, RpcClient client, Exception e) {
						error_num++;
//						e.printStackTrace();
						if(client != null) {
							client.close(e);
						}
						return error_num;
					}
				}).start();
				
				//间接休息,使得搬运工之间不同时搬运
				try {
					Thread.sleep(wait_time/this.workerNum);
				} catch (InterruptedException e) {
				}
			}
			
		}
	}
	
	public void addRequest(RPCMessage msg) {
		//顺序加密，8个节点就要单线程执行8次，效率太低。
		if(this.status) {
			// 设置数字信封 目标机器的公钥被保存在clinet的属性中
//			DataCipher data = new DataCipher(UUID.randomUUID().toString(), msg.getMsg(), this.key.getPrivateKey(),
//					this.serverPublicKey, this.cipher);
//			msg.setMsg(JSON.toJSONString(data));// 更新密文发送

			pool.add(msg);
		}
	}
	
    

	public void stop() {
		this.status = false;
		this.pool.stop();
	}
	


	/**
	 * @Description: 请求任务的消息池
	 * @author xiaoming
	 * @date:上午10:59:04
	 */
	public class RequestPool{
		private boolean status = true;
		
		private LinkedBlockingQueue<RPCMessage> queue = new LinkedBlockingQueue<RPCMessage>();

		public void add(RPCMessage msg) {
			if(this.status) {
				queue.add(msg);
			}
		}
		

		/**
		 * @Description: 批量获取消息列表
		 * @param maxSize 一次性获取最大数
		 * @return
		 * @throws
		 */
		public List<RPCMessage> getMsgList(int maxSize){
			List<RPCMessage> msgList = new ArrayList<RPCMessage>(); 
			for (int i = 0; i < maxSize; i++) {
				RPCMessage msg = queue.poll();

				if (msg != null) {
					msgList.add(msg);
				}
//				else {
//					break;
//				}
			}
//			System.out.println("queue.size=" + queue.size());
			return msgList;
		}

		public void start() {
			this.status = true;
		}
		
		public void stop() {
			this.status = false;
		}
		
	}


}
