package com.hzgc.common.service.facedynrepo;

import java.io.Serializable;

/**
 * 动态库表属性
 */
public class PersonTable implements Serializable {
    //es索引
    public static final String DYNAMIC_INDEX = "dynamicperson";
    //es类型
    public static final String PERSON_INDEX_TYPE = "person";
    //图片的小图ftp地址 xxx/xxx/xxx/
    public static final String SURL = "sftpurl";
    //图片的大图ftp地址 xxx/xxx/xxx/
    public static final String BURL = "bftpurl";
    //float特征值
    public static final String FEAUTRE = "feature";
    //比特特征值
    public static final String BITFEATURE = "bitfeature";
    //设备id
    public static final String IPCID = "ipcid";
    //时间戳 数据格式 xxxx-xx-xx xx:xx:xx(年-月-日 时:分:秒)
    public static final String TIMESTAMP = "timestamp";
    //行人属性：年龄
    public static final String AGE = "age";
    //行人属性：怀抱婴儿
    public static final String BABY = "baby";
    //行人属性：拎东西
    public static final String BAG = "bag";
    //行人属性：下衣颜色
    public static final String BOTTOMCOLOR = "bottomcolor";
    //行人属性：下衣类型
    public static final String BOTTOMTYPE = "bottomtype";
    //行人属性：帽子类型
    public static final String HAT = "hat";
    //行人属性：头发
    public static final String HAIR = "hair";
    //行人属性：背包类型
    public static final String KNAPSACK = "knapsack";
    //行人属性：是否斜挎包
    public static final String MESSENGERBAG = "messengerbag";
    //行人属性：行人方向
    public static final String ORIENTATION = "orientation";
    //行人属性：性别
    public static final String SEX = "sex";
    //行人属性：肩上的包
    public static final String SHOULDERBAG = "shoulderbag";
    //行人属性：雨伞
    public static final String UMBRELLA = "umbrella";
    //行人属性：上衣颜色
    public static final String UPPERCOLOR = "uppercolor";
    //行人属性：上衣类型
    public static final String UPPERTYPE = "uppertype";
    //行人属性：车辆类型
    public static final String CARTYPE = "cartype";

    public static final String HOSTNAME = "hostname";

    public static final String SABSOLUTEPATH = "sabsolutepath";

    public static final String BABSOLUTEPATH = "babsolutepath";

}
