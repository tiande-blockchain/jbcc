package cn.tdchain.jbcc.rpc.io.eclc;

import java.net.Socket;

/**
 * @Description: 包装Socket对象，存储客户端的公钥信息。
 * @author xiaoming
 * @date:下午7:02:30
 */
public class ClientSocket {
	private Socket socket;
	private String clientPubliKey = null;
	
	public ClientSocket(Socket socket, String clientPubliKey) {
		this.socket = socket;
		this.clientPubliKey = clientPubliKey;
	}
	
	public Socket getSocket() {
		return socket;
	}
	public String getClientPubliKey() {
		return clientPubliKey;
	}
	
}
