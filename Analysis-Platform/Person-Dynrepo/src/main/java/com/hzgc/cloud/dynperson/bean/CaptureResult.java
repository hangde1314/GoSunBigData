package com.hzgc.cloud.dynperson.bean;

import lombok.Data;

import java.io.Serializable;

@Data
public class CaptureResult implements Serializable {

    private String searchId;

    private SingleResults singleResults;

}
