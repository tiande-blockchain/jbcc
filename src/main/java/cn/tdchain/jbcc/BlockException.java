package cn.tdchain.jbcc;

public class BlockException extends RuntimeException{
	public BlockException(String msg) {
		super(msg);
	}

	public BlockException(Throwable cause) {
		super(cause);
	}
}
