/*
 * Copyright (c) 2017 Beijing Tiande Technology Co., Ltd.
 * All Rights Reserved.
 */
package cn.tdchain.cipher.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import cn.tdchain.cipher.CipherException;


/**
 * File Utility.
 *
 * @version 2.0
 * @author Houmj 2017-10-10
 */
public final class FileUtils {

    private FileUtils() {
    }

    /**
     * Save file.
     * 
     * @param path String
     * @param value Object
     */
    public static void saveFile(String path, Object value) {

        if (path == null || path.length() == 0) {
            return;
        }

        /* 创建密钥库路径 */
        File ksPath = new File(path);
        ksPath.getParentFile().mkdirs();

        FileOutputStream fos = null;
        ObjectOutputStream oos = null;
        try {
            fos = new FileOutputStream(path, true);
            oos = new ObjectOutputStream(fos);
            oos.writeObject(value);
            oos.flush();
        } catch (Exception e) {
            throw new CipherException("save file to disk error:" + e.getMessage());
        } finally {
            if(oos != null) {
            	try {
					oos.close();
				} catch (IOException e) {
				}
            }
        }
    }

    /**
     * Read file.
     * 
     * @param path String
     * @return object
     */
    public static Object readFile(String path) {
    	synchronized (path) {
    		FileInputStream fis = null;
            ObjectInputStream ois = null;
            Object result = null;
            try {
                fis = new FileInputStream(path);
                ois = new ObjectInputStream(fis);
                result = ois.readObject();
            } catch (Exception e) {
            	throw new CipherException("reader file error:" + e.getMessage());
            } finally {
                if(ois != null) {
                	try {
    					ois.close();
    				} catch (IOException e) {
    				}
                }
            }
            return result;
		}
        
    }

}
