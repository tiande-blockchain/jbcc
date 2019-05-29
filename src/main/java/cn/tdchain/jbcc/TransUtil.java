/*
 * Copyright (c) 2017 Beijing Tiande Technology Co., Ltd.
 * All Rights Reserved.
 */
package cn.tdchain.jbcc;

import cn.tdchain.Trans;
import cn.tdchain.cipher.rsa.Sha256Util;
import cn.tdchain.cipher.utils.HashCheckUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;

/**
 * Trans util
 * @author xiaoming
 * 2019年4月18日
 */
public class TransUtil {
    public final static int HASH_LENGTH = 64;

    /**
     * get a trans hash
     * @param tx
     * @return String
     */
    public static String getTransHash(Trans tx) {
        JSONObject txJsonO = JSONObject.parseObject(JSON.toJSONString(tx));
        txJsonO.remove("hash");//不包含hash字段
        txJsonO.remove("blockHash");//不包含hash字段
        txJsonO.remove("connectionId");//不包含connectionId字段
        txJsonO.remove("status");//不包含status字段
        txJsonO.remove("account");//不包含account字段
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
     * Description: 解析块高度：64位hash"+"height
     * @param hashHeight
     * @return Long
     */
    public static Long getHeight(String hashHeight) {
        if (StringUtils.isBlank(hashHeight) || hashHeight.length() <= HASH_LENGTH)
            throw new RuntimeException("getHeight: split hash height exception, hash string: " + hashHeight);

        Long height = 0L;
        try {
            height = Long.valueOf(hashHeight.substring(HASH_LENGTH));
        } catch (Exception e) {
            throw new RuntimeException("getHeight: split hash height exception, hash string: " + hashHeight);
        }
        return height;
    }

    public static Long getHeight(Trans trans) {
        if (trans == null)
            throw new RuntimeException("trans is null");

        String hashHeight = trans.getHash();
        return getHeight(hashHeight);
    }

    /**
     * Description: 解析hash串：64位hash"+"height
     * @param hashHeight
     * @return String
     */
    public static String getHash(String hashHeight) {
        if (StringUtils.isBlank(hashHeight) || hashHeight.length() <= HASH_LENGTH)
            throw new RuntimeException("split hash string exception, hash string: " + hashHeight);

        String hash = "";
        try {
            hash = hashHeight.substring(0, HASH_LENGTH);
        } catch (Exception e) {
            throw new RuntimeException("getHeight: split hash string exception, hash string: " + hashHeight);
        }

        return hash;
    }

    public static String getHash(Trans trans) {
        if (trans == null)
            throw new RuntimeException("trans is null");

        String hashHeight = trans.getHash();
        return getHash(hashHeight);
    }

    /**
     * hash 规则：64位hash "+" height
     * @param hash
     * @param height
     * @return String
     */
    public static String hashHeight(String hash, Long height) {
        if (StringUtils.isBlank(hash) || !HashCheckUtil.hashCheck(hash))
            throw new TransInfoException("trans hash is error, hash=" + hash);

        if (height == null || height < 0)
            throw new TransInfoException("block height is null or less zero");

        return hash.concat(height + "");
    }

    public static void main(String[] args) {
        String str = "937d45babcf5fac3a3b889957cfd706c8ce1d1e5542acecfca8e1764a3e068b2";
        System.out.println(getHeight(str));
        System.out.println(getHash(str));
    }
}
