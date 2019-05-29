/*
 * Copyright (c) 2017 Beijing Tiande Technology Co., Ltd.
 * All Rights Reserved.
 */
package cn.tdchain;

import cn.tdchain.cipher.utils.HashCheckUtil;
import cn.tdchain.jbcc.DateUtils;
import cn.tdchain.jbcc.SQLCheckUtil;
import cn.tdchain.jbcc.SensitiveWordsUtil;
import cn.tdchain.jbcc.TransInfoException;
import cn.tdchain.jbcc.TransUtil;

/**
 * Description: 天德区块链交易实体
 * @author xiaoming
 * 2019年4月18日
 */
public class Trans extends TransHead {
    protected String account;//客户端账户

    protected String data;//其他交易数据，json object 的字符串结构


    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    /**
     * note: no need to call this method
     */
    @Deprecated
    public void upHash() {
        //验证key不能为null
        if (this.key == null || this.key.trim().length() == 0 || this.key.trim().length() > 255) {
            throw new TransInfoException("key is null or too long.");
        }

        this.setTimestamp(DateUtils.getCurrentTime());

        if (this.hash != null && this.hash.length() > 0 && !this.hash.equals("null")) {
            this.preHash = this.hash;//当前交易hash会变成历史交易hash
        }

        this.hash = computHash(this);
    }

    /**
     * Description: 计算出交易hash
     * @param t
     * @return String
     */
    public String computHash(Trans t) {
        return TransUtil.getTransHash(t);
    }

    public void check() {
        check(false);
    }

    /**
     * Description: 验证自己属性
     */
    public void check(boolean enableVerifySensitiveWorks) {
        // 交易属性验证
        if (this.getKey() == null || this.getKey().length() == 0 || this.getKey().length() > 255) {
            throw new TransInfoException("key is null or too long, max size is 255.");
        }

        // 去掉交易空格
        this.setKey(this.getKey().trim());

        //key不能有特殊字符
        if (HashCheckUtil.illegalCharacterCheck(this.key)) {
            throw new TransInfoException("key have Illegal character.");
        }

        //type字段不能有特殊字符
        if (HashCheckUtil.illegalCharacterCheck(this.type) || (this.type != null && this.type.length() > 45)) {
            throw new TransInfoException("type have Illegal character.");
        }

        if (SQLCheckUtil.checkSQLError(this.getKey())) {
            throw new TransInfoException("key have Illegal character.");
        }

        if (this.getData() == null || this.getData().trim().length() == 0 || this.getData().trim().getBytes().length > 65535) {
            throw new TransInfoException("data is null or too long, max byte size is 65535.");
        }

        //data不能有特殊字符
        if (HashCheckUtil.illegalCharacterCheck(this.data)) {
            throw new TransInfoException("data have Illegal character.");
        }

        if (this.getHash() == null || this.getHash().trim().length() != 64) {
            this.upHash();
        }

        //hash验证
        if (!HashCheckUtil.hashCheck(this.hash)) {
            throw new TransInfoException("hash error:" + this.hash);
        }

        //pre hash验证，要么等于"null"要么就是正常的hash字符串
        if (!HashCheckUtil.hashCheck(this.preHash) && !"".equals(this.preHash)) {
            throw new TransInfoException("pre hash error:" + this.preHash);
        }

        // 验证是否包含敏感词汇
        if (enableVerifySensitiveWorks) {
            checkSensitiveWords();
        }

        //检查超交易生成时间，超过1秒的交易可能存在问题.
        checkStartTime();
    }

    /**
     * Description: 验证交易是否包含敏感词汇
     */
    private void checkSensitiveWords() {
        String key = this.getKey();
        String data = this.getData();
        boolean isContains = SensitiveWordsUtil.containsSensitiveWords(key, SensitiveWordsUtil.MATCH_TYPE_MIN);
        if (isContains) {
            throw new TransInfoException("trans[key=" + key + "] contains sensitive words");
        }
        isContains = SensitiveWordsUtil.containsSensitiveWords(data, SensitiveWordsUtil.MATCH_TYPE_MIN);
        if (isContains) {
            throw new TransInfoException("trans[data=" + data + "] contains sensitive words");
        }
    }

    /**
     * Description: 检查交易是否过时？不能超过6秒之后再提交交易。
     */
    protected void checkStartTime() {
        Long differ_time = (System.currentTimeMillis() - DateUtils.getTime(this.getTimestamp()));
        if ((differ_time > 6000) || differ_time < -6000) {
            //过时交易或者交易时间戳超前
            throw new TransInfoException("the trans out of time or timestamp is error.");
        }
    }

    /**
     * Description: 交易状态
     * @author xiaoming
     * 2019年4月18日
     */
    public enum TransStatus {
        prep, success, failed
    }

}
