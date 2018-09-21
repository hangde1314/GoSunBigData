package com.hzgc.service.dyncar.bean;

import com.hzgc.jniface.CarAttribute;
import lombok.Data;

import java.io.Serializable;

/**
 * 动态图片定义
 */
@Data
public class CapturedPicture implements Serializable {

    //小图url
    private String sabsolutepath;

    //大图url
    private String babsolutepath;

    //捕获照片的设备 id
    private String deviceId;

    //设备名称
    private String deviceName;

    // 时间
    private String timestamp;

    //车辆属性
    private CarAttribute carAttribute;

}
