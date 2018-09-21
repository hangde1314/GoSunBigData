package com.hzgc.common.service.facedynrepo;

import java.io.Serializable;

public class VehicleTable implements Serializable{

    //es索引
    public static final String INDEX = "dynamiccar";
    //es类型
    public static final String TYPE = "car";
    //设备id
    public static final String IPCID = "ipcid";
    //时间戳 数据格式 xxxx-xx-xx xx:xx:xx(年-月-日 时:分:秒)
    public static final String TIMESTAMP = "timestamp";
    //小图路径
    public static final String SFTPURL = "sftpurl";
    //大图路径
    public static final String BFTPURL = "bftpurl";
    //小图本地绝对路径
    public static final String SABSOLUTEPATH = "sabsolutepath";
    //大图本地绝对路径
    public static final String BABSOLUTEPATH = "babsolutepath";
    //主机名称hostname
    public static final String HOSTNAME = "hostname";
    //float特征值
    public static final String FEAUTRE = "feature";
    //比特特征值
    public static final String BITFEATURE = "bitfeature";
    //车的商标
    public static final String BRAND_NAME = "brand_name";
    //车牌
    public static final String PLATE_LICENCE = "plate_licence";
    //分词器
    public static final String IK_SMART = "ik_smart";
    //车辆对象类型
    public static final String VEHICLE_OBJECT_TYPE = "vehicle_object_type";
    //主驾驶安全带
    public static final String BELT_MAINDRIVER = "belt_maindriver";
    //副驾驶安全带
    public static final String BELT_CODRIVER = "belt_codriver";
    //打电话
    public static final String CALL_CODE = "call_code";
    //车辆颜色
    public static final String VEHICLE_COLOR = "vehicle_color";
    //撞损
    public static final String CRASH_CODE = "crash_code";
    //危化品车
    public static final String DANGER_CODE = "danger_code";
    //小物品检测
    public static final String MARKER_CODE = "marker_code";
    //车牌遮挡
    public static final String PLATE_SCHELTER_CODE = "plate_schelter_code";
    //车牌标识
    public static final String PLATE_FLAG_CODE = "plate_flag_code";
    //车牌污损
    public static final String PLATE_DESTAIN_CODE = "plate_destain_code";
    //车牌颜色
    public static final String PLATE_COLOR_CODE = "plate_color_code";
    //车牌类型
    public static final String PLATE_TYPE_CODE = "plate_type_code";
    //行李架
    public static final String RACK_CODE = "rack_code";
    //天窗
    public static final String SUNROOF_CODE = "sunroof_code";
    //车辆类型
    public static final String VEHICLE_TYPE = "vehicle_type";

}
