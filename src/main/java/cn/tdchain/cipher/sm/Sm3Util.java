/*
 * Copyright (c) 2019 Beijing Tiande Technology Co., Ltd.
 * All Rights Reserved.
 */
package cn.tdchain.cipher.sm;

import java.nio.charset.StandardCharsets;
import java.security.Security;

import org.bouncycastle.crypto.digests.SM3Digest;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import cn.tdchain.cipher.utils.ByteUtils;




/**
 * @version 1.0
 * @author jiating 2019-01-21
 */
public class Sm3Util {
    
    static {
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }
    
    public static String hash(String text) {
        
        byte[] srcData = text.getBytes(StandardCharsets.UTF_8);
        SM3Digest digest = new SM3Digest();
        digest.update(srcData, 0, srcData.length);
        byte[] hash = new byte[digest.getDigestSize()];
        digest.doFinal(hash, 0);
        return   ByteUtils.byteToHex(hash);
    }
}
