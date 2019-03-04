package cn.tdchain.jbcc;
/**
 * @Description: 当jbcc请求超时时抛出次异常
 * @author xiaoming
 * @date:上午10:33:24
 */
public class JbccTimeOutException extends RuntimeException{
	public JbccTimeOutException(String msg) {
		super(msg);
	}
}
