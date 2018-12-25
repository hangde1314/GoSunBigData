package com.hzgc.cloud.community.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel(value = "网格人口数量统计出参")
public class GridPeopleCount {
    @ApiModelProperty(value = "总人数")
    private int peopleCount;
    @ApiModelProperty(value = "各标签总人数")
    private List<GridFlagCount> flagCount;
}

