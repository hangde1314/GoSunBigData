package com.hzgc.common.collect.bean;

import com.hzgc.jniface.FaceAttribute;

import java.io.Serializable;

public class FaceObject implements Serializable {
    private String id;                  //对象唯一ID
    private String ipcId;               // 设备ID
    private String timeStamp;           // 时间（格式：2017-01-01 00：00：00）
    private FaceAttribute attribute;    // 人脸属性对象
    private String sFtpUrl;                // 小图ftp路径（带hostname的ftpurl）
    private String bFtpUrl;                // 大图ftp路径（带hostname的ftpurl）
    private String sAbsolutePath;        // 小图存储绝对路径(带ftp根目录)
    private String bAbsolutePath;       //大图存储绝对路径(带ftp根目录)
    private String sRelativePath;       //小图存储绝对路径(不带ftp根目录)
    private String bRelativePath;       //大图存储绝对路径(不带ftp根目录)
    private String hostname;            // 图片保存主机:hostname
    private String ip;                  //ftp服务器ip

    public static FaceObject builder() {
        return new FaceObject();
    }

    public String getIpcId() {
        return ipcId;
    }

    public FaceObject setIpcId(String ipcId) {
        this.ipcId = ipcId;
        return this;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public FaceObject setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
        return this;
    }

    public FaceAttribute getAttribute() {
        return attribute;
    }

    public FaceObject setAttribute(FaceAttribute attribute) {
        this.attribute = attribute;
        return this;
    }

    public String getsFtpUrl() {
        return sFtpUrl;
    }

    public FaceObject setsFtpUrl(String sFtpUrl) {
        this.sFtpUrl = sFtpUrl;
        return this;
    }

    public String getbFtpUrl() {
        return bFtpUrl;
    }

    public FaceObject setbFtpUrl(String bFtpUrl) {
        this.bFtpUrl = bFtpUrl;
        return this;
    }

    public String getsAbsolutePath() {
        return sAbsolutePath;
    }

    public FaceObject setsAbsolutePath(String sAbsolutePath) {
        this.sAbsolutePath = sAbsolutePath;
        return this;
    }

    public String getbAbsolutePath() {
        return bAbsolutePath;
    }

    public FaceObject setbAbsolutePath(String bAbsolutePath) {
        this.bAbsolutePath = bAbsolutePath;
        return this;
    }

    public String getHostname() {
        return hostname;
    }

    public FaceObject setHostname(String hostname) {
        this.hostname = hostname;
        return this;
    }

    public String getId() {
        return id;
    }

    public FaceObject setId(String id) {
        this.id = id;
        return this;
    }

    public FaceObject setIp(String ftpIp) {
        this.ip = ftpIp;
        return this;
    }

    public String getIp() {
        return ip;
    }

    public String getsRelativePath() {
        return sRelativePath;
    }

    public FaceObject setsRelativePath(String sRelativePath) {
        this.sRelativePath = sRelativePath;
        return this;
    }

    public String getbRelativePath() {
        return bRelativePath;
    }

    public FaceObject setbRelativePath(String bRelativePath) {
        this.bRelativePath = bRelativePath;
        return this;
    }
}
