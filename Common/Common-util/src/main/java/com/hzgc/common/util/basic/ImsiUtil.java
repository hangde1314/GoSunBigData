package com.hzgc.common.util.basic;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ImsiUtil {
    public static String toMac(String imsi) {
        String mac;
        try {
            String imsi_32bit = Long.toString(Long.valueOf(imsi), 32).toUpperCase();
            mac = "IM" + "-"
                    + imsi_32bit.substring(0, 2) + "-"
                    + imsi_32bit.substring(2, 4) + "-"
                    + imsi_32bit.substring(4, 6) + "-"
                    + imsi_32bit.substring(6, 8) + "-"
                    + imsi_32bit.substring(8, 10);
        }catch (Exception e){
            log.error("IMSI to MAC failed, IMSI is: ");
            return imsi;
        }
        return mac;
    }
}
