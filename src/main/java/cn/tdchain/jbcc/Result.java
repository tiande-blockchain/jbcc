/*
 *
 *  *   @project        jbcc
 *  *   @file           Result
 *  *   @author         warne
 *  *   @date           19-5-29 下午4:20
 *
 */

package cn.tdchain.jbcc;

import cn.tdchain.jbcc.rpc.RPCResult;

public class Result<T> {
    private RPCResult.StatusType status = RPCResult.StatusType.fail;
    private T entity;
    private String msg;

    public boolean isTimeout() {
        return this.status == RPCResult.StatusType.timeout;
    }

    public boolean isSuccess() {
        return this.status == RPCResult.StatusType.succes;
    }

    public boolean isFail() {
        return this.status == RPCResult.StatusType.fail;
    }

    public RPCResult.StatusType getStatus() {
        return status;
    }

    public void setStatus(RPCResult.StatusType status) {
        this.status = status;
    }

    public T getEntity() {
        return entity;
    }

    public void setEntity(T entity) {
        this.entity = entity;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

}
