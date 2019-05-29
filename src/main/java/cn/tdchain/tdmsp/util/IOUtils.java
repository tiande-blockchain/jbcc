/*
 * Copyright (c) 2017 Beijing Tiande Technology Co., Ltd.
 * All Rights Reserved.
 */
package cn.tdchain.tdmsp.util;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.commons.lang3.StringUtils;


/**
 * IO Utility.
 *
 * @author Houmj 2017-10-19
 * @version 1.0
 */
public final class IOUtils {

    private IOUtils() {
    }

    /**
     * Close InputStream, OutputStream, Reader, Writer, etc.
     * @param closeable InputStream, OutputStream, Reader, Writer, etc
     * @throws IOException IO exception
     */
    public static void close(Closeable closeable) throws IOException {
        if (closeable == null) {
            return;
        }
        closeable.close();
    }

    /**
     * Description: 获得指定文件的byte数组
     * @param filePath
     * @return byte[]
     */
    public static byte[] getBytes(String filePath) {
        byte[] buffer = null;
        try {
            File file = new File(filePath);
            FileInputStream fis = new FileInputStream(file);
            ByteArrayOutputStream bos = new ByteArrayOutputStream(1000);
            byte[] b = new byte[1000];
            int n;
            while ((n = fis.read(b)) != -1) {
                bos.write(b, 0, n);
            }
            fis.close();
            bos.close();
            buffer = bos.toByteArray();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return buffer;
    }

    /**
     * Description: 根据byte数组，生成文件
     * @param bfile
     * @param filePath
     * @param fileName
     */
    public static void generateFile(byte[] bfile, String filePath, String fileName) {
        BufferedOutputStream bos = null;
        FileOutputStream fos = null;
        File file = null;
        try {
            File dir = new File(filePath);
            if (!dir.exists()) {//判断文件目录是否存在
                boolean sucess = dir.mkdirs();
            }
            file = new File(filePath + File.separator + fileName);
            fos = new FileOutputStream(file);
            bos = new BufferedOutputStream(fos);
            bos.write(bfile);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (bos != null) {
                try {
                    bos.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }

    /**
     * Description: 获得指定文件的字符串
     * @param filePath
     * @return String
     */
    public static String getString(String filePath) {
        if (StringUtils.isBlank(filePath)) {
            return null;
        }
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(filePath));
            StringBuilder sb = new StringBuilder();
            while (true) {
                String s = br.readLine();
                if (s == null) {
                    break;
                }
                sb.append(s);
            }
            return sb.toString();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    /**
     * Description: 获得项目下指定文件的字符串
     * @param clazz
     * @param filePath
     * @return String
     */
    public static String getResourcesString(Class clazz, String filePath) {
        if (StringUtils.isBlank(filePath) || clazz == null) {
            return null;
        }
        BufferedReader br = null;
        try {
            InputStream resourceAsStream = clazz.getResourceAsStream(filePath);
            br = new BufferedReader(new InputStreamReader(resourceAsStream));
            StringBuilder sb = new StringBuilder();
            while (true) {
                String s = br.readLine();
                if (s == null) {
                    break;
                }
                sb.append(s);
            }
            return sb.toString();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
