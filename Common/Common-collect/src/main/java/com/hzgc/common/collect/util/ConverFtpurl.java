package com.hzgc.common.collect.util;

public class ConverFtpurl {

    public static String toHttpPath(String ip,String port,String absolutePath){
        StringBuffer stringBuffer = new StringBuffer();
        StringBuffer append = stringBuffer.append("http://")
                                        .append(ip)
                                        .append(":")
                                        .append(port)
                                        .append("?url=")
                                        .append(absolutePath);
        return append.toString();
    }
}
