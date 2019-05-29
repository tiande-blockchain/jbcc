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
 * 2019年4月18日
 */
public class DateUtils {

    public static final String TIME_STAMP_SPACE = "yyyy-MM-dd HH:mm:ss SSS";
    

    private DateUtils() {
    }

    /**
     * Get current time with pattern parameter.
     * @return String
     */
    public static String getCurrentTime() {
    	SimpleDateFormat f = new SimpleDateFormat(TIME_STAMP_SPACE);
    	return f.format(new Date()); 
    }
    
    /**
     * @param data
     * @return long
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
