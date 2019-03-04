package cn.tdchain.jbcc.rpc.nio.handler;

public class NioRpcClientException extends RuntimeException{
    public NioRpcClientException() {
    }

    public NioRpcClientException(String message) {
        super(message);
    }

    public NioRpcClientException(String message, Throwable cause) {
        super(message, cause);
    }

    public NioRpcClientException(Throwable cause) {
        super(cause);
    }
}
