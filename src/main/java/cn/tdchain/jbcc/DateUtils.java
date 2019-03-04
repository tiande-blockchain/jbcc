/*
 * Copyright (c) 2017 Beijing Tiande Technology Co., Ltd.
 * All Rights Reserved.
 */
package cn.tdchain.jbcc;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * Date Utility.
 * @author xiaoming
 * @date: 2018年11月20日 下午2:12:42
 */
public class DateUtils {

    public static final String TIME_STAMP_SPACE = "yyyy-MM-dd HH:mm:ss SSS";
    

    private DateUtils() {
    }

    /**
     * Get current time with pattern parameter.
     * 
     * @param pattern format pattern
     * @return formatted current time
     */
    public static String getCurrentTime() {
    	SimpleDateFormat f = new SimpleDateFormat(TIME_STAMP_SPACE);
    	return f.format(new Date()); 
    }
    
    /**
     * @Title: getCurrentTime   
     * @Description:  
     * @param: @param data
     * @param: @return      
     * @return: long      
     * @throws
     */
    public static long getTime(String data) {
    	try {
    		SimpleDateFormat f = new SimpleDateFormat(TIME_STAMP_SPACE);
			return f.parse(data).getTime();
		} catch (ParseException e) {
			e.printStackTrace();
			return 0;
		} catch (NumberFormatException e) {
			e.printStackTrace();
			return 0;
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		} 
    }
    
    
}
