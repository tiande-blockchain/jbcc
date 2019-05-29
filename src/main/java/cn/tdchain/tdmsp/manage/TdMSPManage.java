/*
 * Copyright (c) 2019 Beijing Tiande Technology Co., Ltd.
 * All Rights Reserved.
 */
package cn.tdchain.tdmsp.manage;

import java.security.KeyStore;
import java.security.cert.X509Certificate;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.Feature;

import cn.tdchain.tdmsp.ca.config.MspConfig;
import cn.tdchain.tdmsp.util.IOUtils;
import cn.tdchain.tdmsp.util.PkiConstant;
import cn.tdchain.tdmsp.util.PkiUtil;

/**
 * @version 1.0
 * @author jiating 2019-01-07
 */
public class TdMSPManage {
    
    private static final String ACLPATH = "./src/main/resources/acl.json";
    private static MspConfig mspConfig;
    private static TdMSPAcl tdMSPAcl;
    
    public TdMSPManage() {
       
    }
    
    
    public MspConfig getMspConfig() {
        return mspConfig;
    }

    public TdMSPAcl getTdMSPAcl() {
        return tdMSPAcl;
    }

    public static TdMSPAcl initMspAcl() {
        
        byte[] jsonByte =  IOUtils.getBytes(ACLPATH);
        
       tdMSPAcl = JSON.parseObject(jsonByte, TdMSPAcl.class, Feature.IgnoreAutoType);
//        log.debug("initMspAcl {}",JSON.toJSONString(tdMSPAcl));
        
        return tdMSPAcl;
    }


    public static MspConfig initLocalMsp() {
        mspConfig =  MspConfig.getInstance();
//        log.debug("init local msp {}",JSON.toJSONString(mspConfig));
        
        return mspConfig;
    }
    
    public static boolean checkCert(MspConfig mspConfig ) {
        boolean result = false;
        
        X509Certificate rootCert = PkiUtil.getCertFromCer(mspConfig.getRootCertPath());
        try {
            KeyStore keyStore = PkiUtil.getKeyStore(mspConfig.getKeyStorePath(), mspConfig.getKeyStorePassword(), PkiConstant.PKCS12);
            X509Certificate  cert =  (X509Certificate) keyStore.getCertificate(mspConfig.getCertAlias());
            
            cert.verify(rootCert.getPublicKey());
            
            result = true;
            
        } catch (Exception e) {
//            log.error(e.getMessage(), e);
                
        }
        
        
        return result;
    }

}
