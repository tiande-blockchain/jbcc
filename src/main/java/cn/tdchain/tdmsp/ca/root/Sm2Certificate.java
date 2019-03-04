/*
 * Copyright (c) 2017-2018 Beijing Tiande Technology Co., Ltd.
 * All Rights Reserved.
 */
package cn.tdchain.tdmsp.ca.root;

import java.security.KeyPair;
import java.security.KeyStore;

//import org.springframework.stereotype.Service;

import cn.tdchain.tdmsp.ca.config.KeyStoreParam;
import cn.tdchain.tdmsp.util.PkiConstant;
import cn.tdchain.tdmsp.util.PkiUtil;


/**
 * RootCertificate.
 *
 * @version 2.0
 * @author Lijiating 2018-10-24
 */
//@Service("sm2Cert")
public class Sm2Certificate extends RsaCertificate {

    /**
     * Create root CA.
     */
    @Override
    public void creatRootCA() {
//        log.info("Create root CA by SM2.");
        createRoot();
    }
    
    @Override
    public void creatOuCert(KeyStoreParam keyStoreParam) {
//        log.info("Create creatOrganizationCert with  keyStoreParam  by SM2.");
        creatOrganizationCert(keyStoreParam);
    }
    
    @Override
    public void creatOuCert(KeyStoreParam keyStoreParam,KeyStore rootkeyStore) {
//        log.info("Create creatOrganizationCert with keyStoreParam,rootkeyStore  by SM2.");
        creatOrganizationCert(keyStoreParam,rootkeyStore);
    }
    
    

    @Override
    protected KeyPair generateKeyPair() throws Exception {
        return PkiUtil.generateSm2KeyPair();
    }

    @Override
    protected String getAlgorithm() {
        return PkiConstant.ALGORITHM_SM2;
    }

}
