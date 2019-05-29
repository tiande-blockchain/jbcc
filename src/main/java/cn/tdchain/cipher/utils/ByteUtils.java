/*
 * Copyright (c) 2017-2018 Beijing Tiande Technology Co., Ltd.
 * All Rights Reserved.
 */
package cn.tdchain.cipher.utils;

import java.math.BigInteger;

/**
 * Byte Utility.
 *
 * @version 2.0
 * @author Houmj 2017-10-09
 */
public final class ByteUtils {

    private ByteUtils() {
    }

    /**
     * Bytes to Hex.
     * 
     * @param value bytes
     * @return String
     */
    public static String byteToHex(byte[] value) {
        if (value == null) {
            return null;
        }
        String hs = "";
        String stmp = "";
        for (int n = 0; n < value.length; n++) {
            stmp = Integer.toHexString(value[n] & 0xff);
            if (stmp.length() == 1) {
                hs = hs + "0" + stmp;
            } else {
                hs = hs + stmp;
            }
        }
        return hs;
    }

    /**
     * To bytes with padding.
     * 
     * @param value
     * @param length
     * @return byte[]
     */
    public static byte[] toBytesPadded(BigInteger value, int length) {
        byte[] result = new byte[length];
        byte[] bytes = value.toByteArray();

        int bytesLength;
        int srcOffset;
        if (bytes[0] == 0) {
            bytesLength = bytes.length - 1;
            srcOffset = 1;
        } else {
            bytesLength = bytes.length;
            srcOffset = 0;
        }

        if (bytesLength > length) {
            throw new RuntimeException(
                    "Input is too large to put in byte array of size "
                            + length);
        }

        int destOffset = length - bytesLength;
        System.arraycopy(bytes, srcOffset, result, destOffset, bytesLength);
        return result;
    }

}
