package com.hzgc.service.community.param;

import com.hzgc.service.people.model.Car;
import com.hzgc.service.people.model.Flag;
import com.hzgc.service.people.model.Imsi;
import lombok.Data;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;

@Data
public class ImportantPeopleRecognize implements Serializable {
    // 重点人员ID
    private String peopleId;
    // 识别记录类型
    private int type;
    // 姓名
    private String name;
    // 身份证
    private String idCard;
    // 车辆列表
    private List<Car> car;
    // imsi列表
    private List<Imsi> imac;
    // 标签列表
    private List<Flag> flag;
    // 最后抓拍时间
    private Timestamp lastTime;
    // 被识别照片ID
    private Long pictureId;
    // 抓拍大图
    private String burl;
    // 抓拍小图
    private String surl;
    // 匹配相似度
    private Float similarity;
    // 抓拍时间
    private Timestamp captureTime;
    // 抓拍设备ID
    private String deviceId;
    //imis码
    private String imsi;
    //mac码
    private String mac;
    //车牌
    private String plate;
}
