package com.hzgc.service.community.param;

import com.hzgc.service.people.param.Flag;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@ApiModel(value = "重点人员识别记录查询出参")
@Data
public class ImportantPeopleRecognizeVO implements Serializable {
    @ApiModelProperty(value = "重点人员ID")
    private String id;
    @ApiModelProperty(value = "识别记录类型")
    private int type;
    @ApiModelProperty(value = "姓名")
    private String name;
    @ApiModelProperty(value = "身份证")
    private String idCard;
    @ApiModelProperty(value = "车辆列表")
    private List<String> car;
    @ApiModelProperty(value = "IMSI列表")
    private List<String> imac;
    @ApiModelProperty(value = "标签列表")
    private List<Flag> flag;
    @ApiModelProperty(value = "最后抓拍时间")
    private String lastTime;
    @ApiModelProperty(value = "人员照片ID")
    private Long peoplePictureId;
    @ApiModelProperty(value = "被设别照片ID")
    private Long pictureId;
    @ApiModelProperty(value = "抓拍大图")
    private String burl;
    @ApiModelProperty(value = "抓拍小图")
    private String surl;
    @ApiModelProperty(value = "匹配相似度")
    private Float similarity;
    @ApiModelProperty(value = "抓拍时间")
    private String captureTime;
    @ApiModelProperty(value = "抓拍设备ID")
    private String deviceId;
    @ApiModelProperty(value = "抓拍设备")
    private String deviceName;
    @ApiModelProperty(value = "IMSI码")
    private String imsi;
    @ApiModelProperty(value = "Mac地址")
    private String mac;
    @ApiModelProperty(value = "车牌号")
    private String plate;
}
