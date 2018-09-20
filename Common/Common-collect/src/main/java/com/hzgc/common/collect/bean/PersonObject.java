package com.hzgc.common.collect.bean;

import com.hzgc.seemmo.bean.personbean.Person;

import java.io.Serializable;

public class PersonObject implements Serializable {
    private String ipcId;               // 设备ID
    private String timeStamp;           // 时间（格式：2017-01-01 00：00：00）
    private Person attribute;           // 行人属性对象
    private String sFtpUrl;                // 小图ftp路径（带hostname的ftpurl）
    private String bFtpUrl;                // 大图ftp路径（带hostname的ftpurl）
    private String sAbsolutePath;        // 小图相对路径（不带ftp根跟路径）
    private String bAbsolutePath;    // 大图相对路径（不带ftp根跟路径）
    private String hostname;            // 图片保存主机:hostname
    private String feature;             //float特征值
    private String bitfeature;          //bit特征值

    public static PersonObject builder() {
        return new PersonObject();
    }

    public String getIpcId() {
        return ipcId;
    }

    public PersonObject setIpcId(String ipcId) {
        this.ipcId = ipcId;
        return this;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public PersonObject setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
        return this;
    }

    public Person getAttribute() {
        return attribute;
    }

    public PersonObject setAttribute(Person attribute) {
        this.attribute = attribute;
        return this;
    }

    public String getHostname() {
        return hostname;
    }

    public PersonObject setHostname(String hostname) {
        this.hostname = hostname;
        return this;
    }

    public String getsFtpUrl() {
        return sFtpUrl;
    }

    public PersonObject setsFtpUrl(String sFtpUrl) {
        this.sFtpUrl = sFtpUrl;
        return this;
    }

    public String getbFtpUrl() {
        return bFtpUrl;
    }

    public PersonObject setbFtpUrl(String bFtpUrl) {
        this.bFtpUrl = bFtpUrl;
        return this;
    }

    public String getsAbsolutePath() {
        return sAbsolutePath;
    }

    public PersonObject setsAbsolutePath(String sAbsolutePath) {
        this.sAbsolutePath = sAbsolutePath;
        return this;
    }

    public String getbAbsolutePath() {
        return bAbsolutePath;
    }

    public PersonObject setbAbsolutePath(String bAbsolutePath) {
        this.bAbsolutePath = bAbsolutePath;
        return this;
    }

    public String getFeature() {
        return feature;
    }

    public void setFeature(String feature) {
        this.feature = feature;
    }

    public String getBitfeature() {
        return bitfeature;
    }

    public void setBitfeature(String bitfeature) {
        this.bitfeature = bitfeature;
    }
}
