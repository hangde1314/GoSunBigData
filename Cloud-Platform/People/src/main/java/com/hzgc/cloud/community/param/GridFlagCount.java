package com.hzgc.cloud.community.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "网格下标签人数统计")
public class GridFlagCount {
    @ApiModelProperty(value = "标签")
    private String flag;
    @ApiModelProperty(value = "人数")
    private int count;
}
