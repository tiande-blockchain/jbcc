/*
 * Copyright (c) 2017 Beijing Tiande Technology Co., Ltd.
 * All Rights Reserved.
 */
package cn.tdchain.jbcc.rpc.io.server;

import cn.tdchain.cipher.Key;
import cn.tdchain.jbcc.net.ConnectionCount;
import cn.tdchain.jbcc.rpc.MessageHandler;
import cn.tdchain.jbcc.rpc.RPCMessage;
import cn.tdchain.jbcc.rpc.RpcServer;
import cn.tdchain.jbcc.rpc.io.eclc.ClientSocket;
import cn.tdchain.jbcc.rpc.io.eclc.EclcServerSocket;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.util.IOUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.SocketException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * rpc server.
 * 
 * @version 2.0
 * @author Xiaoming 2017-12-13
 */
public class IORpcServer implements RpcServer{

    private ExecutorUtil executorUtil = new ExecutorUtil();
    
    private String token = "tiande@123456AbC";
    
    private int port = 18088;

    private MessageHandler handler = null;
    
    private Key key = null;

    /**
     * Constructor.
     * @param port
     * @param handler
     * @param token
     * @param key
     */
    public IORpcServer(int port, MessageHandler handler, String token, Key key) {
        this.port = port;
        this.handler = handler;
        this.token = token;
        this.key = key;
        
        executorUtil.initExecutor();
    }

    /**
     * Start server.
     */
    public void startServer() {
        executorUtil.executor(new Runnable() {
            @Override
            public void run() {
            	EclcServerSocket server = null;
                try {
                    server = new EclcServerSocket(port, token, key);//复杂密码提升网络安全
//                    log.info("Started tdbc rpc server in port: {}.", port);
                   int i = 1;
                    while (true) {
                    	
                    	ClientSocket socket = null;
                    	try {
                    		socket = server.accept2();
                            // 开启新线程去执行请求
                            executorUtil.executor(getTask(socket));
						} catch (SocketException e) {
							if(socket != null) {
								IOUtils.close(socket.getSocket());
							}
						}catch (IOException e) {
							if(socket != null) {
								IOUtils.close(socket.getSocket());
							}
						}catch (Exception e) {
							if(socket != null) {
								IOUtils.close(socket.getSocket());
							}
						}
                    }
                } catch (IOException e) {
                    IOUtils.close(server);
                }
            }
        });
    }

    private Runnable getTask(final ClientSocket socket) {
        Runnable task = new Runnable() {
            @Override
            public void run() {
            	// 输入
                BufferedReader br = null;
                // 输出
	        	BufferedWriter bwrite = null;
	        	String connId = null;
                try {
                    br = new BufferedReader(new InputStreamReader(socket.getSocket().getInputStream()));
                    bwrite = new BufferedWriter(new OutputStreamWriter(socket.getSocket().getOutputStream()));
					while (true) {

						String line = br.readLine();
						
						if (line == null || line.length() == 0) {
							break;
						}
						
						// 执行业务处理，出现任何异常都不能影响通信。
						String result = null;
						RPCMessage msg = null;
						try {
							msg = JSONObject.parseObject(line, RPCMessage.class);
						} catch (Exception e) {
							e.printStackTrace();
						}

                        String connectionId = msg.getSender();
                        connId = connectionId;
                        if (RPCMessage.TargetType.BATCH_RESPHONE == msg.getTargetType()) {
                            Thread.currentThread().setPriority(10);//最高优先级
							String sendPublicKey = socket.getClientPubliKey();
							// 客户端是响应的连接，此时server 进入死循环寻找对应的result对象返回给对应客户端。
							int index = 0;
							while (true) {
								index++;
								//这里不需要处理异常,有异常则退出
								result = handler.getResultMapByConnectionId(connectionId, sendPublicKey);
								//如果有返回则写回客户端，如果出现io异常可能是网络出现问题，则断开连接。
								if (result != null || index == 100) {
									if(result == null) {
										result = "null";
									}
									index = 0;
									bwrite.write(result);
									bwrite.newLine();
									bwrite.flush();
								}

								//休息便可继续去
								try {
									Thread.sleep(30);
								} catch (InterruptedException e) {
								}
							}


						} else {
							// 一般是request的请求
							try {
								result = handler.handler(msg, connectionId);
							} catch (Exception e) {
								//业务处理异常不影响通信
								e.printStackTrace();
							}
							
						}
						
						//如果有返回则写回客户端，如果出现io异常可能是网络出现问题，则断开连接。
						if (result != null) {
							bwrite.write(result);
							bwrite.newLine();
							bwrite.flush();
						}
						
					}
                } catch (SocketException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    IOUtils.close(socket.getSocket());
                    ConnectionCount.newInstance().remove(connId);
                }
                
            }
        };
        return task;
    }
}

/**
 * 线程池配置.
 * 
 * @author Xiaoming 2017-04-20
 */
class ExecutorUtil {

    private Executor executor = null;

    public void initExecutor() {
    	ExecutorService cachedThreadPool = Executors.newFixedThreadPool(200);
        this.executor = cachedThreadPool;
    }

    /**
     * 异步执行.
     * 
     * @param task
     * @author xiaoming
     */
    public void executor(Runnable task) {
        executor.execute(task);
    }

    public Executor getExecutor() {
        return executor;
    }

    public void setExecutor(Executor executor) {
        this.executor = executor;
    }
}
