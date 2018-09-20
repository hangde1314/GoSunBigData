package com.hzgc.collect.service.parser;

import java.io.Serializable;

public class FtpPathMetaData implements Serializable {

    private String ipcid;
    private String timeStamp;

    public String getIpcid() {
        return ipcid;
    }

    void setIpcid(String ipcid) {
        this.ipcid = ipcid;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

}
