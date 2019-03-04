/*
 * Copyright (c) 2017-2018 Beijing Tiande Technology Co., Ltd.
 * All Rights Reserved.
 */
package cn.tdchain.cipher.rsa;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.Security;

import org.bouncycastle.jcajce.provider.digest.Keccak;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import cn.tdchain.cipher.utils.ByteUtils;


/**
 * SHA Utility.
 *
 * @version 2.0
 * @author Lijiating 2018-07-18
 */
public final class ShaUtil {

    private static final Charset UTF8 = StandardCharsets.UTF_8;
    private static ThreadLocal<Keccak.DigestKeccak> digestThreadLocal = new ThreadLocal<Keccak.DigestKeccak>();
    static {
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    private ShaUtil() {
    }

    /**
     * Hash text.
     * 
     * @param text plain text
     * @return hash text
     */
    public static String hash(String text) {
        byte[] digest = null;
        byte[] input = text.getBytes(UTF8);
        digest = sha3(input, 0, input.length);
        return ByteUtils.byteToHex(digest);
    }

    /**
     * Hash text.
     * 
     * @param input plain byte text
     * @return hash text
     */
    public static byte[] hash(byte[] input) {
        return sha3(input, 0, input.length);
    }

    /**
     * Keccak-256 hash function.
     *
     * @param input binary encoded input data
     * @param offset of start of data
     * @param length of data
     * @return hash value
     */
    private static byte[] sha3(byte[] input, int offset, int length) {
        Keccak.DigestKeccak kecc = digestThreadLocal.get();
        if (kecc == null) {
            kecc = new Keccak.Digest256();
            digestThreadLocal.set(kecc);
        }
        kecc.update(input, offset, length);
        return kecc.digest();
    }

}
