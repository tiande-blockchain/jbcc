/*
 * Copyright (c) 2019 Beijing Tiande Technology Co., Ltd.
 * All Rights Reserved.
 */
package cn.tdchain.tdmsp.manage;

import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 *
 * @version 1.0
 * @author jiating 2019-01-08
 */
public class TdMSPAcl {
    private ArrayList<String> crlList = new ArrayList<String>();
    private ArrayList<String> ouList = new ArrayList<String>();
    private ArrayList<Policies> policiesList = new ArrayList<Policies>();
    private HashMap<String , ArrayList<String>> aclMap = new HashMap<String , ArrayList<String>>();
    
    
    public TdMSPAcl() {
       
    }
    public ArrayList<String> getCrlList() {
        return crlList;
    }
    public void setCrlList(ArrayList<String> crlList) {
        this.crlList = crlList;
    }
    public ArrayList<String> getOuList() {
        return ouList;
    }
    public void setOuList(ArrayList<String> ouList) {
        this.ouList = ouList;
    }
    public ArrayList<Policies> getPoliciesList() {
        return policiesList;
    }
    public void setPoliciesList(ArrayList<Policies> policiesList) {
        this.policiesList = policiesList;
    }
    public HashMap<String, ArrayList<String>> getAclMap() {
        return aclMap;
    }
    public void setAclMap(HashMap<String, ArrayList<String>> aclMap) {
        this.aclMap = aclMap;
    }
   
    
    
    
    
    
}
