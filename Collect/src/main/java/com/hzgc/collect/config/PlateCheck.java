package com.hzgc.collect.config;


import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class PlateCheck {
    private Map <String, Map <String, Long>> map = new ConcurrentHashMap <>();

    public boolean plateCheck(String ipcid, String plate) {
        if (null != ipcid && map.containsKey(ipcid)) {
            Map <String, Long> longMap = map.get(ipcid);
            if (null != plate && longMap.containsKey(plate)) {
                Long aLong = longMap.get(plate);
                if ((System.currentTimeMillis() - aLong) > 3600000) {
                    //更新时间，返回添加
                    longMap.put(plate, System.currentTimeMillis());
                    return true;
                } else {
                    //更新时间，返回不添加
                    longMap.put(plate, System.currentTimeMillis());
                    return false;
                }
            } else {
                //新增
                longMap.put(plate, System.currentTimeMillis());
                return true;
            }
        } else {
            //新增
            HashMap <String, Long> hashMap = new HashMap <>();
            hashMap.put(plate, System.currentTimeMillis());
            map.put(ipcid, hashMap);
            return true;
        }
    }
}
