package com.hzgc.common.service.facedynrepo;

import java.io.Serializable;

/**
 * 动态库表属性
 */
public class FaceTable implements Serializable {
    //es索引
    public static final String DYNAMIC_INDEX = "dynamicface";
    //es类型
    public static final String PERSON_INDEX_TYPE = "face";
    //小图片的ftp地址 xxx/xxx/xxx/
    public static final String SFTPURL = "sftpurl";
    //大图片的ftp地址 xxx/xxx/xxx/
    public static final String BFTPURL = "bftpurl";
    //设备id
    public static final String IPCID = "ipcid";
    //主机名称hostname
    public static final String HOSTNAME = "hostname";
    //大图本地绝对路径
    public static final String BABSOLUTEPATH = "babsolutepath";
    //小图本地绝对路径
    public static final String SABSOLUTEPATH = "sabsolutepath";
    //时间戳 数据格式 xxxx-xx-xx xx:xx:xx(年-月-日 时:分:秒)
    public static final String TIMESTAMP = "timestamp";
    //人脸属性-是否戴眼镜
    public static final String EYEGLASSES = "eyeglasses";
    //人脸属性-性别 男或女
    public static final String GENDER = "gender";
    //人脸属性-年龄 1-150
    public static final String AGE = "age";
    //人俩属性-是否有口罩
    public static final String MASK = "mask";
    //人脸属性-胡子类型
    public static final String HUZI = "huzi";
    //人脸清晰度
    public static final String SHARPNESS = "sharpness";
    //小文件合并后数据表
    public static final String PERSON_TABLE = "person_table";
    //小文件合并前数据表
    public static final String MID_TABLE = "mid_table";
    //float特征值
    public static final String FEATURE = "feature";
    //bit特征值
    public static final String BITFEATURE = "bitfeature";
    //特征值比对结果相似度
    public static final String SIMILARITY = "similarity";
    //hive中特征值比对udf函数
    public static final String FUNCTION_NAME = "compare";
    //图片分组ID
    public static final String GROUP_FIELD = "picid";

}
