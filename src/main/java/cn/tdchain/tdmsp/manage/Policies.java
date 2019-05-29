/*
 * Copyright (c) 2019 Beijing Tiande Technology Co., Ltd.
 * All Rights Reserved.
 */
package cn.tdchain.tdmsp.manage;

import java.util.ArrayList;

import com.alibaba.fastjson.JSON;

import cn.tdchain.tdmsp.ca.config.PoliciesRule;
import cn.tdchain.tdmsp.ca.config.PoliciesType;

/**
 * @version 1.0
 * @author jiating 2019-01-08
 */
public class Policies {
    private String policiesName;
    private PoliciesType policiesType;
    private PoliciesRule policiesRule;
    private ArrayList<String> policiesList;
    
    public Policies() {
        
    }
    
    public String getPoliciesName() {
        return policiesName;
    }
    public void setPoliciesName(String policiesName) {
        this.policiesName = policiesName;
    }
    public PoliciesType getPoliciesType() {
        return policiesType;
    }
    public void setPoliciesType(PoliciesType policiesType) {
        this.policiesType = policiesType;
    }
    public PoliciesRule getPoliciesRule() {
        return policiesRule;
    }
    public void setPoliciesRule(PoliciesRule policiesRule) {
        this.policiesRule = policiesRule;
    }

   

    public ArrayList<String> getPoliciesList() {
        return policiesList;
    }

    public void setPoliciesList(ArrayList<String> policiesList) {
        this.policiesList = policiesList;
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
    
}
