package cn.tdchain.tdmsp.util;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

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
     *
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
     * 获得指定文件的byte数组 
     */  
    public static  byte[] getBytes(String filePath){  
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
     * 根据byte数组，生成文件 
     */  
    public static void generateFile(byte[] bfile, String filePath,String fileName) {  
        BufferedOutputStream bos = null;  
        FileOutputStream fos = null;  
        File file = null;  
        try {  
            File dir = new File(filePath);
            if(!dir.exists()){//判断文件目录是否存在
               boolean sucess =  dir.mkdirs();
            }
            file = new File(filePath+File.separator+fileName);  
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

}
