/*
 * Copyright (c) 2019 Beijing Tiande Technology Co., Ltd.
 * All Rights Reserved.
 */
package cn.tdchain.tdmsp.ca.config;


import java.util.Properties;

/**
 * Description: 枚举单例
 * @version 1.0
 * @author jiating 2019-01-02
 */
public class MspConfig {
    private static Properties prop = null;
    private static String PROPPATH = "/msp.properties";
    
    private String rootCertPath;
    private String keyStorePath;
    private String keyStorePassword;
    private String certAlias;
    private String privateKeyAlias;
    private String privateKeyPassword;
    
    
    private MspConfig() {
         prop = new Properties();
        try {
            prop.load(this.getClass().getResourceAsStream(PROPPATH));
       } catch (Exception e) {
          e.printStackTrace();
       }
    }
    
    public static MspConfig getInstance(){
        return EnumSystemConfig.INSTANCE.getInstance();
    }
    
    private static enum EnumSystemConfig{
        INSTANCE;
        
        private MspConfig mspConfig;
        //JVM会保证此方法绝对只调用一次
        private EnumSystemConfig(){
            mspConfig = new MspConfig();
        }
        public MspConfig getInstance(){
            return mspConfig;
        }
    }
    
   
    public String getRootCertPath() {
        return prop.getProperty("tdchain.msp.cert.root");
    }


    public String getKeyStorePath() {
        return  prop.getProperty("tdchain.msp.local.keyStore");
    }

    public String getKeyStorePassword() {
        return prop.getProperty("tdchain.msp.local.keyStore.ksPassword");
    }


    public String getCertAlias() {
        return prop.getProperty("tdchain.msp.local.cert.alias");
    }


    public String getPrivateKeyAlias() {
        return prop.getProperty("tdchain.msp.local.privateKeyAlias");
    }


    public String getPrivateKeyPassword() {
        return prop.getProperty("tdchain.msp.local.privateKeyPassword");
    }
    
}
