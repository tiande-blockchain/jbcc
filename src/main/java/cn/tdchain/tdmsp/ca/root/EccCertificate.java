/*
 * Copyright (c) 2017-2018 Beijing Tiande Technology Co., Ltd.
 * All Rights Reserved.
 */
package cn.tdchain.tdmsp.ca.root;

import java.security.KeyPair;
import java.security.KeyStore;


import cn.tdchain.tdmsp.ca.config.SystemConfig;
import cn.tdchain.tdmsp.util.PkiConstant;
import cn.tdchain.tdmsp.util.PkiUtil;

/**
 * RootCertificate.
 * @version 1.0
 * @author Lijiating 2018-10-24
 */
public class EccCertificate extends RsaCertificate {

    /**
     * Create root CA.
     */
    @Override
    public void creatRootCA(String ksPath, String passwd) {
//        log.info("Create root CA by ECC.");
        createRoot(ksPath, passwd);
    }
    
    @Override
    public void creatOuCert(SystemConfig keyStoreParam) {
//        log.info("Create creatOrganizationCert with  keyStoreParam  by ECC.");
        creatOrganizationCert(keyStoreParam);
    }
    
    @Override
    public void creatOuCert(SystemConfig keyStoreParam, KeyStore rootkeyStore) {
//        log.info("Create creatOrganizationCert with keyStoreParam,rootkeyStore  by ECC.");
        creatOrganizationCert(keyStoreParam, rootkeyStore);
    }
    

    @Override
    protected KeyPair generateKeyPair() throws Exception {
        return PkiUtil.generateEccKeyPair();
    }
    
    
    @Override
    protected String getAlgorithm() {
        return PkiConstant.ALGORITHM_ECC;
    }

}
