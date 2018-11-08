package com.hzgc.service.dispatch.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@ApiModel(value = "布控信息展示返回封装")
@Data
public class DispatchVO implements Serializable {
    @ApiModelProperty(value = "布控ID")
    @NotNull
    private String id;
    @ApiModelProperty(value = "区域ID")
    @NotNull
    private Long regionId;
    @ApiModelProperty(value = "区域名字")
    @NotNull
    private String regionName;
    @ApiModelProperty(value = "姓名")
    private String name;
    @ApiModelProperty(value = "身份证")
    private String idCard;
    @ApiModelProperty(value = "阈值")
    private Float threshold;
    @ApiModelProperty(value = "布控车辆")
    private String car;
    @ApiModelProperty(value = "布控MAC")
    private String mac;
    @ApiModelProperty(value = "备注")
    private String notes;
    @ApiModelProperty(value = "布控状态")
    @NotNull
    private Integer status;
    @ApiModelProperty(value = "创建时间")
    private String createTime;
    @ApiModelProperty(value = "更新时间")
    private String updateTime;
}
