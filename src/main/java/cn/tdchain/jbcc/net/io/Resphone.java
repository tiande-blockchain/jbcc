/*
 * Copyright (c) 2017 Beijing Tiande Technology Co., Ltd.
 * All Rights Reserved.
 */
package cn.tdchain.jbcc.net.io;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;

import cn.tdchain.cipher.Cipher;
import cn.tdchain.cipher.DataCipher;
import cn.tdchain.cipher.Key;
import cn.tdchain.jbcc.net.io.IONet.Task;
import cn.tdchain.jbcc.rpc.RPCMessage;
import cn.tdchain.jbcc.rpc.RPCResult;
import cn.tdchain.jbcc.rpc.exception.RpcException;
import cn.tdchain.jbcc.rpc.io.client.RpcClient;

/**
 * Description: 异步提交请求消息
 * @author xiaoming
 * 2019年4月18日
 */
public class Resphone{
	private Task task;

	private String ip;

	private int workerNum = 1;// 工人数，默认1名.

	private ResphonePool pool;

	private Cipher cipher;

	private Key key;

	private boolean status = true;// true=在线     false=死忙的

	private String connectionId;

	private String serverPublicKey;


	public Resphone(Task task, String serverIp, String connectionId, Key key, Cipher cipher, int workerNum, String serverPublicKey) {
		this.task = task;
		this.ip = serverIp;
		this.connectionId = connectionId;
		this.key = key;
		this.cipher = cipher;
		if(workerNum > this.workerNum) {
			this.workerNum = workerNum;
		}
		this.serverPublicKey = serverPublicKey;

		this.pool = new ResphonePool(task);
	}


	public void start() {
		this.pool.start(); //开启消息池的异步任务

		if(this.status) {
			long wait_time = 20;
			// 开始异步批量提交请求
			for(int i = 0; i < this.workerNum; i++) {
				new Thread(new Runnable() {

					@Override
					public void run() {
						int error_num = 0;
						while(true) {

							if(status) {
								/** 继续搬运消息列表给server */

									//封装批量消息体
									RPCMessage batch_msg = new RPCMessage();
									batch_msg.setTarget(ip);
									batch_msg.setTargetType(RPCMessage.TargetType.BATCH_RESPHONE);
									batch_msg.setSender(connectionId);//

									//发送如果出现网络异常超过三次则判断为该节点下线。结束本任务
									RpcClient client = null;
									try {
										//获取连接对象
										client = task.getClient();

										while(status) {
											try {
												// 发送告知server 我是响应客户端
												client.send(batch_msg.toJsonString());

												//接着死循接受响应结果，
												while(true) {
													String r_str = client.readResult();
													if ("null".equals(r_str)){
													    continue;
                                                    }
													//处理结果
													if(r_str != null && r_str.length() > 0) {
														try {
															RPCResult<String> r = JSONObject.parseObject(r_str, RPCResult.class);

															if(r != null && r.getType() == RPCResult.ResultType.batch_resphone) {
																//解密获取明文
																String cipher_data = r.getEntity();//密文DataCipher
																DataCipher data = JSONObject.parseObject(cipher_data, DataCipher.class);
														        String data_str = data.getData(key.getPrivateKey(), cipher);

																if(data_str == null) {
																	//解密失败或者返null
																	continue;//退出本次循环
																}

																Map<String, RPCResult> connectionMap = JSON.parseObject(data_str, new TypeReference<Map<String, RPCResult>>(){});
																if(connectionMap != null && connectionMap.size() > 0) {

																	pool.add(connectionMap);//把结果添加到池中
																}
															}

														} catch (Exception e) {
															//业务异常不影响通信
															e.printStackTrace();
														}
													}

													//重置error_num = 0
													error_num = 0;
												}



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
											}


											if(error_num > 5) {
												task.stop();//可能出现网络异常，需要结束整个任务。
											}
										}


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
									}


									if(error_num > 5) {
										task.stop();//可能出现网络异常，需要结束整个任务。
									}

							}else {
								break;//结束搬运工作
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
					Thread.sleep(wait_time / this.workerNum);
				} catch (InterruptedException e) {
				}
			}

		}

	}

	public void stop() {
		this.status = false;
		this.pool.stop();
	}

	/**
	 * Description: 根据消息id弹出消息结果，如果没有则返回空。
	 * @param messageId
	 * @return RPCResult
	 */
	public RPCResult poll(String messageId) {
		return this.pool.poll(messageId);
	}

	/**
	 * Description: 返回结果池
	 * @author xiaoming
	 * 2019年4月18日
	 */
	public class ResphonePool{
		private Task task = null;

		private boolean status = true;// true=在线     false=死忙的

		public ResphonePool(Task task) {
			this.task = task; //注入本次task对象
		}

		private ConcurrentHashMap<String, RPCResult> resphonePool = new ConcurrentHashMap<String, RPCResult>();

		/**
		 * Description: 根据消息id获取返回结果对象
		 *
		 * @param messageId
		 * @return
		 */
		public  RPCResult poll(String messageId) {
			try {
				return resphonePool.remove(messageId);
			} catch (Exception e) {
			}
			return null;
		}

		private synchronized void add(Map<String, RPCResult> connectionMap) {
			resphonePool.putAll(connectionMap);
		}

		public void stop() {
			this.status = false;
		}

		public void start() {
			this.status = true;
			// 开始异步扫描pool中过时的result对象
			checkTimeOut(4000);
		}

		/**
		 * Description: 异步扫描过时的结果对象
		 * @param timeOut
		 */
		private void checkTimeOut(long timeOut) {
			new Thread(new Runnable() {

				@Override
				public void run() {
					while(true) {
						if(status) {
							long now_time = System.currentTimeMillis();
							try {
								/** 异常类型消息检查 */
								RPCResult error_r = resphonePool.remove(RPCResult.PRC_RESULT_DESCRYPT_ERROR);
								if((error_r != null) && (error_r.getType() == RPCResult.ResultType.msg_error)) {
									if(error_r.getStatus() == RPCResult.StatusType.fail) {
										//收到此返回消息说明服务端解密出现异常，可能连接对象不对称，需要关闭连接后重新连接。
										task.stop();//整个任务结束
										return;//随后结束线程
									}
								}


								resphonePool.forEach((k,v) -> {
									if (v != null) {
										// 消除过时的 RPCResult 对象
										if ((now_time - v.getStartTime()) > timeOut) {
											resphonePool.remove(v.getMessageId());
										}
									}

								});


							} catch (Exception e) {
								e.printStackTrace();
							}
						}else {
							break;//结束本循环工作。
						}

						//4秒循环坚检查一次
						try {
							Thread.sleep(timeOut * 2);
						} catch (InterruptedException e) {
						}
					}
				}
			}).start();
		}

	}


}
