/*
 * Copyright (c) 2017 Beijing Tiande Technology Co., Ltd.
 * All Rights Reserved.
 */
package cn.tdchain.tdmsp.util;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.Provider;
import java.security.SecureRandomSpi;
import java.security.Security;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

/**
 * Implementation from <a href=
 * "https://github.com/bitcoinj/bitcoinj/blob/master/core/src/main/java/org/bitcoinj/crypto/LinuxSecureRandom.java">BitcoinJ
 * implementation</a>
 *
 * <p>
 * A SecureRandom implementation that is able to override the standard JVM provided implementation,
 * and which simply serves random numbers by reading /dev/urandom. That is, it delegates to the
 * kernel on UNIX systems and is unusable on other platforms. Attempts to manually set the seed are
 * ignored. There is no difference between seed bytes and non-seed bytes, they are all from the same
 * source.
 * 
 * @version 1.0
 * @author jiating 2018-07-19
 */
public class LinuxSecureRandom extends SecureRandomSpi {

    private static final long serialVersionUID = 208702708372565523L;

    private static final FileInputStream RANDOM;
    private final DataInputStream dis;

    static {
        try {
            File file = new File("/dev/urandom");
            // This stream is deliberately leaked.
            RANDOM = new FileInputStream(file);
            if (RANDOM.read() == -1) {
                throw new RuntimeException("/dev/urandom not readable?");
            }
            // Now override the default SecureRandom implementation with this one.
            int position = Security
                    .insertProviderAt(new LinuxSecureRandomProvider(), 1);

            if (position != -1) {
//                log.info("Secure randomness will be read from {} only.", file);
            } else {
//                log.info("Randomness is already secure.");
            }
        } catch (FileNotFoundException e) {
            // Should never happen.
//            log.error("/dev/urandom does not appear to exist or is not openable");
            throw new RuntimeException(e);
        } catch (IOException e) {
//            log.error("/dev/urandom does not appear to be readable");
            throw new RuntimeException(e);
        }
    }

    /**
     * Constructor.
     */
    public LinuxSecureRandom() {
        // DataInputStream is not thread safe, so each random object has its own.
        dis = new DataInputStream(RANDOM);
    }

    @Override
    protected void engineSetSeed(byte[] bytes) {
        // Ignore.
    }

    @Override
    protected void engineNextBytes(byte[] bytes) {
        try {
            dis.readFully(bytes); // This will block until all the bytes can be read.
        } catch (IOException e) {
            throw new RuntimeException(e); // Fatal error. Do not attempt to recover from this.
        }
    }

    @Override
    protected byte[] engineGenerateSeed(int i) {
        byte[] bits = new byte[i];
        engineNextBytes(bits);
        return bits;
    }

    /**
     * Provider.
     *
     * @version 1.0
     * @author jiating 2018-07-19
     */
    private static class LinuxSecureRandomProvider extends Provider {
        private static final long serialVersionUID = -432355574793128305L;

        /**
         * Constructor.
         */
        LinuxSecureRandomProvider() {
            super("LinuxSecureRandom", 1.0,
                    "A Linux specific random number provider that uses /dev/urandom");
            put("SecureRandom.LinuxSecureRandom",
                    LinuxSecureRandom.class.getName());
        }
    }
}
