/*
 * Copyright (c) 2017 Beijing Tiande Technology Co., Ltd.
 * All Rights Reserved.
 */
package cn.tdchain.cipher.rsa;

import java.lang.reflect.Array;

/**
 * Array Utility.
 *
 * @version 2.0
 * @author Xiaoming 2017-02-04
 */
public class ArrayUtils {

    private ArrayUtils() {
    }

    /**
     * Subarray.
     * 
     * @param data byte[]
     * @param startIndexInclusive int
     * @param endIndexExclusive int
     * @return byte[]
     */
    public static byte[] subarray(final byte[] data, int startIndexInclusive,
                                  int endIndexExclusive) {
        if (data == null) {
            return null;
        }

        if (startIndexInclusive < 0) {
            startIndexInclusive = 0;
        }

        if (endIndexExclusive > data.length) {
            endIndexExclusive = data.length;
        }

        final int newSize = endIndexExclusive - startIndexInclusive;

        final Class<?> type = data.getClass().getComponentType();

        if (newSize <= 0) {
            final byte[] emptyArray = (byte[]) Array.newInstance(type, 0);
            return emptyArray;
        }

        final byte[] subarray = (byte[]) Array.newInstance(type, newSize);
        System.arraycopy(data, startIndexInclusive, subarray, 0, newSize);
        return subarray;
    }

    /**
     * Ad all.
     * 
     * @param enBytes byte[]
     * @param tmpData bytes
     * @return byte[]
     */
    public static byte[] addAll(final byte[] enBytes, final byte... tmpData) {

        if (enBytes == null) {
            return clone(tmpData);
        } else if (tmpData == null) {
            return clone(enBytes);
        }

        final Class<?> type1 = enBytes.getClass().getComponentType();

        final byte[] joinedArray = (byte[]) Array.newInstance(type1,
                enBytes.length + tmpData.length);
        System.arraycopy(enBytes, 0, joinedArray, 0, enBytes.length);
        try {
            System.arraycopy(tmpData, 0, joinedArray, enBytes.length,
                    tmpData.length);
        } catch (final ArrayStoreException ase) {

            // Check if problem was due to incompatible types

            /*
             * 
             * We do this here, rather than before the copy because:
             * 
             * - it would be a wasted check most of the time
             * 
             * - safer, in case check turns out to be too strict
             * 
             */
            final Class<?> type2 = tmpData.getClass().getComponentType();

            if (!type1.isAssignableFrom(type2)) {

                throw new IllegalArgumentException(
                        "Cannot store " + type2.getName() + " in an array of "
                                + type1.getName(),
                        ase);
            }
            throw ase; // No, so rethrow original
        }
        return joinedArray;
    }

    /**
     * Clone.
     * 
     * @param enBytes byte[]
     * @return byte[]
     */
    public static byte[] clone(final byte[] enBytes) {
        if (enBytes == null) {
            return null;
        }
        return enBytes.clone();
    }
}
