package com.hzgc.common.collect.bean;

import com.hzgc.seemmo.bean.carbean.Vehicle;

import java.io.Serializable;

public class CarObject implements Serializable {
    private String ipcId;               // 设备ID
    private String timeStamp;           // 时间（格式：2017-01-01 00：00：00）
    private Vehicle attribute;          // 车辆属性对象
    private String sFtpUrl;                // 小图ftp路径（带hostname的ftpurl）
    private String bFtpUrl;                // 大图ftp路径（带hostname的ftpurl）
    private String sAbsolutePath;        // 小图相对路径（不带ftp根跟路径）
    private String bAbsolutePath;    // 大图相对路径（不带ftp根跟路径）
    private String hostname;            // 图片保存主机:hostname

    public static CarObject builder() {
        return new CarObject();
    }

    public String getIpcId() {
        return ipcId;
    }

    public CarObject setIpcId(String ipcId) {
        this.ipcId = ipcId;
        return this;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public CarObject setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
        return this;
    }

    public Vehicle getAttribute() {
        return attribute;
    }

    public CarObject setAttribute(Vehicle attribute) {
        this.attribute = attribute;
        return this;
    }

    public String getHostname() {
        return hostname;
    }

    public CarObject setHostname(String hostname) {
        this.hostname = hostname;
        return this;
    }

    public String getsFtpUrl() {
        return sFtpUrl;
    }

    public CarObject setsFtpUrl(String sFtpUrl) {
        this.sFtpUrl = sFtpUrl;
        return this;
    }

    public String getbFtpUrl() {
        return bFtpUrl;
    }

    public CarObject setbFtpUrl(String bFtpUrl) {
        this.bFtpUrl = bFtpUrl;
        return this;
    }

    public String getsAbsolutePath() {
        return sAbsolutePath;
    }

    public CarObject setsAbsolutePath(String sAbsolutePath) {
        this.sAbsolutePath = sAbsolutePath;
        return this;
    }

    public String getbAbsolutePath() {
        return bAbsolutePath;
    }

    public CarObject setbAbsolutePath(String bAbsolutePath) {
        this.bAbsolutePath = bAbsolutePath;
        return this;
    }
}
