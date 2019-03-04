/*
 * Copyright (c) 2017 Beijing Tiande Technology Co., Ltd.
 * All Rights Reserved.
 */
package cn.tdchain.cipher.rsa;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import cn.tdchain.cipher.utils.ByteUtils;


/**
 * SHA-256 Utility.
 * @version 2.0
 * @author Limd 2017-03-21
 */
public class Sha256Util {

    private static ThreadLocal<MessageDigest> digestThreadLocal = new ThreadLocal<MessageDigest>(); // cipher线程局部变量

    private Sha256Util() {
    }

    /**
     * Hash text.
     * 
     * @param text plain text
     * @return hash text
     */
    public static String hash(String text) {
        byte[] digest = null;
        try {
            MessageDigest md = digestThreadLocal.get();
            if (md == null) {
                md = MessageDigest.getInstance("SHA-256");
                digestThreadLocal.set(md);
            }
            md.update(text.getBytes(StandardCharsets.UTF_8));
            digest = md.digest();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        
        return ByteUtils.byteToHex(digest);
    }

}
