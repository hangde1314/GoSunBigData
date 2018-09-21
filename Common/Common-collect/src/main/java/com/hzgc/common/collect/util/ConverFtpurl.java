package com.hzgc.common.collect.util;

public class ConverFtpurl {

    public static String toHttpPath(String ip, String port, String absolutePath) {
        return "http://" +
                ip +
                ":" +
                port +
                "/image" +
                "?url=" +
                absolutePath;
    }
}
