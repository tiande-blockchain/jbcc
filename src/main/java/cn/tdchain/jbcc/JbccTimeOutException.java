package cn.tdchain.jbcc;
/**
 * Description: 当jbcc请求超时时抛出次异常
 * @author xiaoming
 * 2019年4月18日
 */
public class JbccTimeOutException extends RuntimeException{
	public JbccTimeOutException(String msg) {
		super(msg);
	}
}
