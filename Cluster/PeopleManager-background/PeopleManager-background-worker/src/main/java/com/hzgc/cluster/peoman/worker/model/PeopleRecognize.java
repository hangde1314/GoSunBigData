package com.hzgc.cluster.peoman.worker.model;

import lombok.Data;

import java.util.Date;

@Data
public class PeopleRecognize {
    private String id;
    private String peopleid;
    private Long community;
    private Long pictureid;
    private String deviceid;
    private Date capturetime;
    private String surl;
    private String burl;
    private Integer flag;
    private Float similarity;
    private Integer filterTime;
}