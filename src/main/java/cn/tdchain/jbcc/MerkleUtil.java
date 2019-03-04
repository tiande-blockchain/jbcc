/*
 * Copyright (c) 2017 Beijing Tiande Technology Co., Ltd.
 * All Rights Reserved.
 */
package cn.tdchain.jbcc;

import java.util.Collections;
import java.util.List;

/**
 * Merkle util
 * @author xiaoming
 * @date: 2018年10月31日 下午2:13:18
 */
public class MerkleUtil {
	
	/**
	 * @Title: getMerkleRoot   
	 * @Description: get a merkle root String  
	 * @param: @param hashList
	 * @param: @return      
	 * @return: String      
	 * @throws
	 */
	public static String getMerkleRoot(List<String> hashList) {
		String merkleRoot = "";
        if (!hashList.isEmpty()) {
            Collections.sort(hashList);//排序交易hash list
            if (hashList.size() == 1) { // MerkleTree必须有2个节点以上才能生成
                merkleRoot = hashList.get(0);
            } else if (hashList.size() > 1) {
                MerkleTree merkle = new MerkleTree(hashList);
                merkleRoot = merkle.getRoot().getBinaryNodeSig();
            }
        }
        
        return merkleRoot;
	}
}
