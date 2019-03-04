/*
 * Copyright (c) 2019 Beijing Tiande Technology Co., Ltd.
 * All Rights Reserved.
 */
package cn.tdchain.tdmsp.ca.config;


import java.util.Properties;

/**
 * 枚举单例
 * 参考：https://www.cnblogs.com/yangzhilong/p/6148639.html
 * @version 1.0
 * @author jiating 2019-01-02
 */
public class SystemConfig {
    private static Properties prop = null;
    private  String issuerdn;
    private  String alias;
    private  String privateKeyAlias;
    private  String privateKeyPassword;
    private  String ksPassword;
    private  String ksPath;
    private  String ksFileName;
    private  String validTime;
    private  String interValidTime;
    private  String certValidTime;
    
    private SystemConfig() {
         prop = new Properties();
        try {
            prop.load(this.getClass().getResourceAsStream("/application.properties"));
       } catch (Exception e) {
          e.printStackTrace();
       }
    }
    
    public static SystemConfig getInstance(){
        return EnumSystemConfig.INSTANCE.getInstance();
    }
    
    private static enum EnumSystemConfig{
        INSTANCE;
        
        private SystemConfig systemConfig;
        //JVM会保证此方法绝对只调用一次
        private EnumSystemConfig(){
            systemConfig = new SystemConfig();
        }
        public SystemConfig getInstance(){
            return systemConfig;
        }
    }
    
    
    
   
    public String getAlias() {
        return prop.getProperty("tdchain.pki.ca.root.alias");
    }

    public String getPrivateKeyAlias() {
        return prop.getProperty("tdchain.pki.ca.root.privateKeyAlias");
    }

    public String getPrivateKeyPassword() {
        return prop.getProperty("tdchain.pki.ca.root.privateKeyPassword");
    }

    public String getKsPassword() {
        return prop.getProperty("tdchain.pki.ca.root.ksPassword");
    }

    public String getKsPath() {
        return prop.getProperty("tdchain.pki.ca.root.ksPath");
    }

    public String getKsFileName() {
        return prop.getProperty("tdchain.pki.ca.root.ksFileName");
    }

  
    public String getValidTime() {
        return prop.getProperty("tdchain.pki.ca.root.validTime");
    }

    public String getInterValidTime() {
        return prop.getProperty("tdchain.pki.ca.root.interValidTime");
    }

    public String getCertValidTime() {
        return prop.getProperty("tdchain.pki.ca.root.certValidTime");
    }

    public String getIssuerdn() {
        return prop.getProperty("tdchain.pki.ca.root.issuerdn");
    }

   
    
    
}
