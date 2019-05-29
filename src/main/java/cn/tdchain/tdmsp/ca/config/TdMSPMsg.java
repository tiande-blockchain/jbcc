/*
 * Copyright (c) 2019 Beijing Tiande Technology Co., Ltd.
 * All Rights Reserved.
 */
package cn.tdchain.tdmsp.ca.config;

import java.io.Serializable;

/**
 * @version 1.0
 * @author jiating 2019-01-08
 */
public class TdMSPMsg implements Serializable {
    
    private static final long serialVersionUID = 334945933165716949L;
    
    private int type = 6;//0 sucess, 默认是错误的
    private String message;
    
    public TdMSPMsg() {
        
    }
    
    public int getType() {
        return type;
    }
    public void setType(int type) {
        this.type = type;
    }
    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }
    
    
}
