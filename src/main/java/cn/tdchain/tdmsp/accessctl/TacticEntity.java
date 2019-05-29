/*
 * Copyright (c) 2017 Beijing Tiande Technology Co., Ltd.
 * All Rights Reserved.
 */
package cn.tdchain.tdmsp.accessctl;

import org.apache.commons.collections.CollectionUtils;

import java.util.List;

/**
 * 权限策略
 */
public class TacticEntity {
    private List<String> writeTrans;
    private List<String> admins;

    public static boolean check(TacticEntity entity) {
        if (entity == null) {
            return false;
        }
        if (CollectionUtils.isEmpty(entity.admins)) {
            return false;
        }
        if (CollectionUtils.isEmpty(entity.writeTrans)) {
            return false;
        }
        return true;
    }

    public List<String> getWriteTrans() {
        return writeTrans;
    }

    public void setWriteTrans(List<String> writeTrans) {
        this.writeTrans = writeTrans;
    }

    public List<String> getAdmins() {
        return admins;
    }

    public void setAdmins(List<String> admins) {
        this.admins = admins;
    }
}
