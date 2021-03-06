package com.hzgc.cloud.people.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@ApiModel(value = "人口库模糊查询出参")
@Data
public class SearchPeopleVO implements Serializable {
    @ApiModelProperty(value = "总条数")
    private int total;
    @ApiModelProperty(value = "人员信息列表")
    private List<PeopleVO> peopleVOList;
}
