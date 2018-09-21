package com.hzgc.service.dynperson.bean;

import com.hzgc.jniface.PersonAttributes;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class Pictures implements Serializable {

    private String babsolutepath;

    private String deviceId;

    private String deviceName;

    private String sabsolutepath;

    private String time;

    private List<PersonAttributes> personAttributes;

    private String bHttpPath;

    private String sHttpPath;

}
