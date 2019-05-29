/*
 * Copyright (c) 2017 Beijing Tiande Technology Co., Ltd.
 * All Rights Reserved.
 */
package cn.tdchain.jbcc;

/**
 * function：description
 * datetime：2019-04-17 17:18
 * author：warne
 */
public abstract class SoutUtil {

    /**
     * 是否打印 System.out.print
     * @return boolean
     */
    public final static boolean isOpenSout() {
        return ConnectionFactory.soutSwitch;
    }

}
