/*
 * Copyright (c) 2019 Beijing Tiande Technology Co., Ltd.
 * All Rights Reserved.
 */
package cn.tdchain.tdmsp.manage;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

import cn.tdchain.cipher.rsa.RsaUtil;
import cn.tdchain.cipher.sm.Sm2Util;
import cn.tdchain.tdmsp.ca.config.TdMSPMsg;
import cn.tdchain.tdmsp.util.EccUtil;
import cn.tdchain.tdmsp.util.PkiConstant;

/**
 * @version 1.0
 * @author jiating 2019-01-08
 */
public class TdMSPIdentity {
    public TdMSPMsg validate(String msg,String signMsg,X509Certificate cert,String method,TdMSPAcl tdMSPAcl,X509Certificate rootCert,String cipherType) {
        TdMSPMsg tdMSPMsg = new TdMSPMsg();
        
        //校验证书
        tdMSPMsg = validateCert(rootCert, cert);
         if(tdMSPMsg.getType() != 0 ) {
             return tdMSPMsg;
         }
         
         //检验签名
         tdMSPMsg = verifySignMsg(msg,signMsg,cert,cipherType);
         if(tdMSPMsg.getType() != 0 ) {
             return tdMSPMsg;
         }
         
         //检验crl
         tdMSPMsg = checkCRL(tdMSPAcl,cert);
         if(tdMSPMsg.getType() != 0 ) {
             return tdMSPMsg;
         }
        
         //检验ou
         String ou = getOUFromCert(cert);
//         tdMSPMsg = checkOU(tdMSPAcl,cert);
         tdMSPMsg = checkOU(tdMSPAcl,ou);
         if(tdMSPMsg.getType() != 0 ) {
             return tdMSPMsg;
         }
         
       //检验acl
         tdMSPMsg = checkAcl(tdMSPAcl,method,ou);
         if(tdMSPMsg.getType() != 0 ) {
             return tdMSPMsg;
         }
        
        
        return tdMSPMsg;
    }

    /**
     * @param tdMSPAcl
     * @param method
     * @param cert
     * @return
     */
    public TdMSPMsg checkAcl(TdMSPAcl tdMSPAcl, String method,
                              String ou) {
        TdMSPMsg tdMSPMsg = new TdMSPMsg();
        tdMSPMsg.setType(3);
        tdMSPMsg.setMessage("No matching organization Unit");
        
        ArrayList<Policies> aclPolicies =  tdMSPAcl.getPoliciesList();
        ArrayList<String> mothodPolicies = getPoliciesByMethod(tdMSPAcl,method);
        if(null == mothodPolicies) {
            tdMSPMsg.setType(2);
            tdMSPMsg.setMessage("getPoliciesByMethod is null");
            return tdMSPMsg;
        }
        
        for (int i = 0; i < mothodPolicies.size(); i++) {
            String mothodPoliciesName =  mothodPolicies.get(i);
            for (int j = 0; j < aclPolicies.size(); j++) {
                Policies policies =   aclPolicies.get(j);
                if(mothodPoliciesName.equals(policies.getPoliciesName())) {
                    
                    if(policies.getPoliciesList().contains(ou)) {
//                        log.debug("find ou containsKey in the policies {}",policies.getPoliciesName());
                        tdMSPMsg.setType(0);
                        tdMSPMsg.setMessage("SUCESS");
                        return tdMSPMsg;
                    }
                    
                }
                
            }
            
        }
        
//        log.debug("Can't  find ou containsKey in the policies {}");
        
        return tdMSPMsg;
    }



    /**
     * @param tdMSPAcl
     * @param method
     * @return ArrayList<String>
     */
    public ArrayList<String> getPoliciesByMethod(TdMSPAcl tdMSPAcl,
                                                  String method) {
         HashMap<String, ArrayList<String>> map = tdMSPAcl.getAclMap();
         if(null == map) {
             return null;
         }
         
        return map.get(method);
    }

    
    /**
     * @param tdMSPAcl
     * @param ou
     * @return TdMSPMsg
     */
    public TdMSPMsg checkOU(TdMSPAcl tdMSPAcl, String  ou) {
        TdMSPMsg tdMSPMsg = new TdMSPMsg();
       
        if(tdMSPAcl.getOuList().contains(ou)) {
            tdMSPMsg.setType(0);
            tdMSPMsg.setMessage("SUCESS");
        }else {
            tdMSPMsg.setType(1);
            tdMSPMsg.setMessage("The certificate Organization Unit Non-existent ");
        }
        
        return tdMSPMsg;
    }

    /**
     * @param tdMSPAcl
     * @param cert
     * @return TdMSPMsg
     */
    public TdMSPMsg checkOU(TdMSPAcl tdMSPAcl, X509Certificate cert) {
        TdMSPMsg tdMSPMsg = new TdMSPMsg();
        
        String ou = getOUFromCert(cert);
        if(tdMSPAcl.getOuList().contains(ou)) {
            tdMSPMsg.setType(0);
            tdMSPMsg.setMessage("SUCESS");
        }else {
            tdMSPMsg.setType(1);
            tdMSPMsg.setMessage("The certificate Organization Unit Non-existent ");
        }
        
        return tdMSPMsg;
    }

    /**
     * @param cert
     * @return String
     */
    public String getOUFromCert(X509Certificate cert) {
        String ou = "";
        
        String subjectName = cert.getSubjectX500Principal().getName();
        subjectName = subjectName.split("OU=")[1];
        ou = subjectName.substring(0, subjectName.indexOf(","));
        
        return ou;
    }

    /**
     * @param tdMSPAcl
     * @param cert
     */
    public TdMSPMsg checkCRL(TdMSPAcl tdMSPAcl, X509Certificate cert) {
        TdMSPMsg tdMSPMsg = new TdMSPMsg();
        String serialNumber =  cert.getSerialNumber().toString();
        
        if(tdMSPAcl.getCrlList().contains(serialNumber)) {
            tdMSPMsg.setType(1);
            tdMSPMsg.setMessage("The certificate was revoked");
        }else {
            tdMSPMsg.setType(0);
            tdMSPMsg.setMessage("SUCESS");
        }
        
        
        return tdMSPMsg;
    }

    /**
     * Description: 验证信息的签名是否匹配
     * @param msg
     * @param signMsg
     * @param cert
     * @param cipherType
     * @return TdMSPMsg
     */
    public TdMSPMsg verifySignMsg(String msg, String signMsg,
                                   X509Certificate cert,String cipherType) {
        TdMSPMsg tdMSPMsg = new TdMSPMsg();
        
        boolean verifyRsult = false;
        if(PkiConstant.RSA.equals(cipherType)) {
            try {
                verifyRsult =  RsaUtil.verify(cert.getPublicKey(), signMsg, msg);
            } catch (Exception e) {
               throw new RuntimeException(e.getMessage());
            }
            
        }else if(PkiConstant.SM2.equals(cipherType)){
            verifyRsult =  Sm2Util.verify(cert.getPublicKey(), signMsg, msg);
        }else if(PkiConstant.ECC.equals(cipherType)){
            verifyRsult =  EccUtil.verify(msg,signMsg,cert.getPublicKey());
        } else {
            tdMSPMsg.setType(1);
            tdMSPMsg.setMessage("it won't support this cipherType ");
            return tdMSPMsg;
        }
        
        
//        log.debug("verifySignMsg {}",verifyRsult);
        
        if(verifyRsult) {
            tdMSPMsg.setType(0);
            tdMSPMsg.setMessage("SUCESS");
        }else {
            tdMSPMsg.setType(1);
            tdMSPMsg.setMessage("verifySignMsg faiure");
        }
        
        
        return tdMSPMsg;
    }

    /**
     * Description:校验证书是否由相应的根证书生成
     * @param rootCert
     * @param cert
     * @return TdMSPMsg
     */
    @SuppressWarnings("finally")
    public TdMSPMsg validateCert(X509Certificate rootCert,X509Certificate cert) {
        TdMSPMsg tdMSPMsg = new TdMSPMsg();
        try {
            
            cert.verify(rootCert.getPublicKey());
            
            tdMSPMsg.setType(0);
            tdMSPMsg.setMessage("SUCESS");
            
        } catch (InvalidKeyException e) {
//            log.error(e.getMessage(), e);
            tdMSPMsg.setType(1);
            tdMSPMsg.setMessage(e.getMessage());
            	
        } catch (CertificateException e) {
//            log.error(e.getMessage(), e);
            tdMSPMsg.setType(2);
            tdMSPMsg.setMessage(e.getMessage());
            	
        } catch (NoSuchAlgorithmException e) {
//            log.error(e.getMessage(), e);
            tdMSPMsg.setType(3);
            tdMSPMsg.setMessage(e.getMessage());
            	
        } catch (NoSuchProviderException e) {
//            log.error(e.getMessage(), e);
            tdMSPMsg.setType(4);
            tdMSPMsg.setMessage(e.getMessage());
            	
        } catch (SignatureException e) {
//            log.error(e.getMessage(), e);
            tdMSPMsg.setType(5);
            tdMSPMsg.setMessage(e.getMessage());
            	
        }finally {
            return tdMSPMsg;
        }
        
    }
}
