package com.hzgc.collect.expand.parser;

import com.hzgc.collect.expand.util.CollectProperties;

public abstract class AbstractParser implements Parser {
    /**
     * 通过上传文件路径解析到ftpUrl
     *
     * @param filePath ftp接收数据路径
     * @return 带IP的ftpUrl
     */
    @Override
    public String getFtpUrl_ip(String filePath) {
        return "ftp://" + CollectProperties.getFtpIp() + ":" + CollectProperties.getFtpPort() + filePath;
    }

    /**
     * 通过上传文件路径解析到ftpUrl（ftp发送至kafka的key）
     *
     * @param filePath ftp接收数据路径
     * @return 带hostname的ftpUrl
     */
    @Override
    public String getFtpUrl_hostname(String filePath) {
        return "ftp://" + CollectProperties.getHostname() + ":" + CollectProperties.getFtpPort() + filePath;
    }
}
