package com.hzgc.cloud.community.model;

import java.io.Serializable;

public class Count implements Serializable {
    private int flagid;
    private int count;

    public int getFlagid() {
        return flagid;
    }

    public void setFlagid(int flagid) {
        this.flagid = flagid;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
