/*
 * Copyright (c) 2017 Beijing Tiande Technology Co., Ltd.
 * All Rights Reserved.
 */
package cn.tdchain.jbcc;

import cn.tdchain.cipher.utils.HashCheckUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import cn.tdchain.Trans;
import cn.tdchain.cipher.rsa.Sha256Util;

/**
 * Trans util
 *
 * @author xiaoming
 * @date: 2018年10月31日 下午3:33:26
 */
public class TransUtil {

    /**
     * @throws
     * @Title: getTransHash
     * @Description: get a trans hash
     * @param: @param tx
     * @param: @return
     * @return: String
     */
    public static String getTransHash(Trans tx) {
        JSONObject txJsonO = JSONObject.parseObject(JSON.toJSONString(tx));
        txJsonO.remove("hash");//不包含hash字段
        txJsonO.remove("blockHash");//不包含hash字段
        txJsonO.remove("connectionId");//不包含connectionId字段
        txJsonO.remove("status");//不包含status字段
        txJsonO.remove("msg");//不包含msg字段
        txJsonO.remove("index");//不包含index字段
        txJsonO.remove("preHash");//不包含preHash字段
        txJsonO.remove("author");//不包含author字段
        txJsonO.remove("version");//不包含version字段
        txJsonO.remove("height");//不包含height字段
        String txJsonStr = txJsonO.toJSONString();
        String hash = Sha256Util.hash(txJsonStr);
        return hash;
    }

    /**
     * 根据heightHash 获取hash
     * @param heightHash
     * @return
     * @throws Exception
     */
    public static Long getHeightByHeightHash(String heightHash) throws Exception {
        if (heightHash != null && heightHash.length() > 0) {
            String[] str = heightHash.split("_");
            if (str.length == 2) {
                return Long.valueOf(str[0]);
            }
            throw new Exception("get height by heightHash=" + heightHash + " error");
        }
        throw new Exception("get height by heightHash=" + heightHash + " error, heightHash is null or empty");
    }

    /**
     * 根据heightHash 获取height
     * @param heightHash
     * @return
     * @throws Exception
     */
    public static String getHashByHeightHash(String heightHash) throws Exception {
        if (heightHash != null && heightHash.length() > 0) {
            String[] str = heightHash.split("_");
            if (str.length == 2) {
                return str[1];
            }
            return heightHash;
        }
        throw new Exception("get hash by heightHash=" + heightHash + " error, heightHash is null or empty");
    }

    /**
     * 设置heightHash
     * @param height
     * @param hash
     * @return
     * @throws Exception
     */
    public static String setHeightHash(Long height, String hash) throws Exception {
        if (height == null || height <= 0) {
            throw new Exception("set heightHash is error, height=" + height + " is null or less than 0");
        }
        if (!HashCheckUtil.hashCheck(hash)) {
            throw new Exception("set heightHash is error, hash=" + hash + " is null or illegal");
        }
        return height + "_" + hash;
    }

}
