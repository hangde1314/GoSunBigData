package com.hzgc.collect.expand.receiver;

import com.hzgc.collect.expand.parser.Parser;

public class Event {
    private String ipcId;                // ipc id
    private String timeStamp;            // 抓拍时间 (2018-08-10 17:13:22)
    private String bFtpUrl;      //带hostname的大图ftpUrl)
    private String sFtpUrl;      //带hostname的小图ftpUrl)
    private String sIpcFtpUrl;   //带ip的小图ftpUrl)
    private String bAbsolutePath;     // 大图存储绝对路径(带ftp根目录)
    private String sAbsolutePath;     // 小图存储绝对路径(带ftp根目录)
    private String hostname;             // 图片保存主机:hostname
    private Parser parser;              //对应设备解析器

    public static Event builder(){
        return new Event();
    }

    public String getIpcId() {
        return ipcId;
    }

    public Event setIpcId(String ipcId) {
        this.ipcId = ipcId;
        return this;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public Event setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
        return this;
    }

    public String getHostname() {
        return hostname;
    }

    public Event setHostname(String hostname) {
        this.hostname = hostname;
        return this;
    }

    public String getbAbsolutePath() {
        return bAbsolutePath;
    }

    public Event setbAbsolutePath(String bAbsolutePath) {
        this.bAbsolutePath = bAbsolutePath;
        return this;
    }

    public Parser getParser() {
        return parser;
    }

    public Event setParser(Parser parser) {
        this.parser = parser;
        return this;
    }

    public String getbFtpUrl() {
        return bFtpUrl;
    }

    public Event setbFtpUrl(String bFtpUrl) {
        this.bFtpUrl = bFtpUrl;
        return this;
    }

    public String getsFtpUrl() {
        return sFtpUrl;
    }

    public Event setsFtpUrl(String sFtpUrl) {
        this.sFtpUrl = sFtpUrl;
        return this;
    }

    public String getsAbsolutePath() {
        return sAbsolutePath;
    }

    public Event setsAbsolutePath(String sAbsolutePath) {
        this.sAbsolutePath = sAbsolutePath;
        return this;
    }

    public String getsIpcFtpUrl() {
        return sIpcFtpUrl;
    }

    public Event setsIpcFtpUrl(String sIpcFtpUrl) {
        this.sIpcFtpUrl = sIpcFtpUrl;
        return this;
    }
}
