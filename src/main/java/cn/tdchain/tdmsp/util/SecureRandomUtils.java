/*
 * Copyright (c) 2017 Beijing Tiande Technology Co., Ltd.
 * All Rights Reserved.
 */
package cn.tdchain.tdmsp.util;

import java.security.SecureRandom;

/**
 * Utility class for working with SecureRandom implementation.
 *
 * @version 1.0
 * @author jiating 2018-07-19
 */
public final class SecureRandomUtils {

    private static final SecureRandom SECURE_RANDOM;

    private static int isAndroid = -1;

    static {
        if (isAndroidRuntime()) {
            new LinuxSecureRandom();
        }
        SECURE_RANDOM = new SecureRandom();
    }

    private SecureRandomUtils() {
    }

    /**
     * Get Secure random.
     * 
     * @return SecureRandom
     */
    public static SecureRandom secureRandom() {
        return SECURE_RANDOM;
    }

    private static boolean isAndroidRuntime() {
        if (isAndroid == -1) {
            final String runtime = System.getProperty("java.runtime.name");
            isAndroid = (runtime != null && runtime.equals("Android Runtime"))
                    ? 1 : 0;
        }
        return isAndroid == 1;
    }

}
