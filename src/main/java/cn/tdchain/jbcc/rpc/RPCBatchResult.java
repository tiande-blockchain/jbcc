package cn.tdchain.jbcc.rpc;

import cn.tdchain.jbcc.Result;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;

import java.util.ArrayList;
import java.util.List;

/**
 * rpc 返回对象
 * connectionId 标识返回的目的地
 * messageId 标识着返回的阻塞线程
 * startTime 标识着该对象的生命起始时间，一旦超过周期范围则被系统丢弃。
 *
 * @author xiaoming
 * 2019年4月18日
 */
public class RPCBatchResult {
    private String messageId;
    private boolean isTimeOut;
    private boolean isFail;
    private List<Result> list = new ArrayList<>();
    private Result result;
    private String msg;


    public static RPCBatchResult newInstance() {
        return new RPCBatchResult();
    }

    public RPCBatchResult add(Result result) {
        this.list.add(result);
        return this;
    }

    public RPCBatchResult result(Result result) {
        this.result = result;
        return this;
    }

    public RPCBatchResult messageId(String messageId) {
        this.messageId = messageId;
        return this;
    }

    public RPCBatchResult msg(String msg) {
        this.msg = msg;
        return this;
    }

    public RPCBatchResult isTimeOut(boolean isTimeOut) {
        this.isTimeOut = isTimeOut;
        if (isTimeOut == true) {
            this.isFail = true;
        }
        return this;
    }

    public RPCBatchResult isFail(boolean isFail) {
        this.isFail = isFail;
        return this;
    }

    public boolean isFail() {
        return isFail;
    }

    public int size() {
        return this.list.size();
    }

    public Result getResult() {
        if (result != null) {
            return result;
        }
        result = new Result();
        result.setMsg(msg);
        if (isTimeOut) {
            result.setStatus(RPCResult.StatusType.timeout);
        }
        return result;
    }

    public <T> List<Result<T>> buildList(TypeReference<T> typeReference) {
        List<Result<T>> nresult = new ArrayList<>();

        for (Result result : list) {
            if (result != null) {
                if (result.getEntity() != null && !isCount(result.getEntity())) {
                    Object o = JSON.parseObject(((JSON) result.getEntity()).toJSONString(), typeReference);
                    result.setEntity(o);
                }
                nresult.add(result);
            }
        }
        return nresult;
    }

    private boolean isCount(Object o) {
        if (o instanceof Integer || o instanceof Long) {
            return true;
        }
        return false;
    }
}
