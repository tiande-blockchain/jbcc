/*
 * Copyright (c) 2017-2018 Beijing Tiande Technology Co., Ltd.
 * All Rights Reserved.
 */
package cn.tdchain.tdmsp.util;

/**
 * Constant.
 *
 * @version 2.0
 * @author Houmj 2018-10-15
 */
public final class PkiConstant {

    public static final String ALGORITHM_RSA = "SHA256withRSA";
    public static final String ALGORITHM_ECC = "SHA256withECDSA";
    public static final String ALGORITHM_SM2 = "SM3withSM2";
    public static final String SM2P256V1 = "sm2p256v1";

    public static final String RSA = "RSA";
    public static final String SM2 = "SM2";
    public static final String ECC = "ECC";
    public static final String AES = "AES";

    public static final String ECDSA = "ECDSA";
    public static final String ECIES = "ECIES";
    public static final String EC = "EC";
    public static final String SHA256 = "SHA256";
    public static final String X509 = "X509";
    public static final String SECP256K1 = "secp256k1";

    public static final String JCEKS = "JCEKS";
    public static final String PKCS12 = "PKCS12";

    private PkiConstant() {
    }

    /**
     * Get algorithm by cipher type.
     * 
     * @param cipherType RSA/SM2/ECC
     * @return String
     */
    public static String getAlgorithm(String cipherType) {
        if (cipherType == null) {
            return null;
        }
        switch (cipherType) {
            case RSA:
                return ALGORITHM_RSA;
            case SM2:
                return ALGORITHM_SM2;
            case ECC:
                return ALGORITHM_ECC;
            default:
                return null;
        }
    }
}
