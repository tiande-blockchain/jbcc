package cn.tdchain;

import cn.tdchain.jbcc.rpc.RPCResult;
import com.alibaba.fastjson.JSONObject;

public class RPCResultJSONObject {

    public static <T> RPCResult<T> parseObject(String source, Class<T> tClass) {
        if (source == null) {
            return null;
        }
        try {
            RPCResult<T> result = JSONObject.parseObject(source, RPCResult.class);
            Object entity = result.getEntity();
            if (entity == null) {
                return result;
            }
            T t = JSONObject.parseObject(entity.toString(), tClass);
            result.setEntity(t);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static <T> RPCResult<T> parseObject(RPCResult source, Class<T> tClass) {
        if (source == null) {
            return null;
        }
        try {
            RPCResult<T> result = source;
            Object entity = result.getEntity();
            if (entity == null) {
                return result;
            }
            T t = JSONObject.parseObject(entity.toString(), tClass);
            result.setEntity(t);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
