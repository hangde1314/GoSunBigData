package com.hzgc.cloud.community.controller;

import com.hzgc.cloud.community.param.GridPeopleCount;
import com.hzgc.cloud.community.param.PeopleCountVO;
import com.hzgc.cloud.community.param.PeopleDTO;
import com.hzgc.cloud.community.param.PeopleVO;
import com.hzgc.cloud.community.service.PeopleCountService;
import com.hzgc.common.service.error.RestErrorCode;
import com.hzgc.common.service.response.ResponseResult;
import com.hzgc.common.service.rest.BigDataPath;
import com.hzgc.common.util.json.JacksonUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Slf4j
@Api(value = "/community", tags = "人员统计查询服务")
public class PeopleCountController {
    @Autowired
    private PeopleCountService peopleCountService;

    @ApiOperation(value = "小区人口数量统计", response = PeopleCountVO.class)
    @RequestMapping(value = BigDataPath.COMMUNITY_COUNT, method = RequestMethod.GET)
    public ResponseResult<PeopleCountVO> countCommunityPeople(Long communityId) {
        if (communityId == null) {
            log.error("Start count community people info, but community id is null");
            return ResponseResult.error(RestErrorCode.ILLEGAL_ARGUMENT, "小区ID不能为空，请检查！");
        }
        log.info("Start count community people info, community id is:" + communityId);
        PeopleCountVO peopleCountVO = peopleCountService.countCommunityPeople(communityId);
        log.info("Count community people info successfully");
        return ResponseResult.init(peopleCountVO);
    }

    @ApiOperation(value = "网格人口数量统计", response = GridPeopleCount.class)
    @RequestMapping(value = BigDataPath.GRID_COUNT, method = RequestMethod.GET)
    public ResponseResult<GridPeopleCount> countGridPeople(Long gridCode) {
        if (gridCode == null) {
            log.error("Start count grid people info, but gridCode is null");
            return ResponseResult.error(RestErrorCode.ILLEGAL_ARGUMENT, "网格ID不能为空，请检查！");
        }
        log.info("Start count grid people info, gridCode is:" + gridCode);
        GridPeopleCount count = peopleCountService.countGridPeople(gridCode);
        log.info("Count grid people info successfully");
        return ResponseResult.init(count);
    }

    @ApiOperation(value = "小区实有人口查询", response = PeopleVO.class)
    @RequestMapping(value = BigDataPath.COMMUNITY_PEOPLE, method = RequestMethod.POST)
    public ResponseResult<List<PeopleVO>> searchCommunityPeople(@RequestBody PeopleDTO param) {
        if (param == null) {
            log.error("Start search community people, but param is null");
            return ResponseResult.error(RestErrorCode.ILLEGAL_ARGUMENT, "查询参数为空，请检查！");
        }
        if (param.getCommunityId() == null) {
            log.error("Start search community people, but communityId is null");
            return ResponseResult.error(RestErrorCode.ILLEGAL_ARGUMENT, "小区ID不能为空，请检查！");
        }
        if (param.getStart() < 0) {
            log.error("Start search community people, but start < 0");
            return ResponseResult.error(RestErrorCode.ILLEGAL_ARGUMENT, "起始行数不能小于0,请检查！");
        }
        if (param.getLimit() <= 0) {
            log.error("Start search community people, but limit <= 0");
            return ResponseResult.error(RestErrorCode.ILLEGAL_ARGUMENT, "分页行数不能小于或等于0，请检查！");
        }
        log.info("Start search community people, param is:" + JacksonUtil.toJson(param));
        List<PeopleVO> peopleVO = peopleCountService.searchCommunityPeople(param);
        log.info("Search community people successfully");
        return ResponseResult.init(peopleVO);
    }

    @ApiOperation(value = "小区重点人口查询", response = PeopleVO.class)
    @RequestMapping(value = BigDataPath.COMMUNITY_PEOPLE_IMPORTANT, method = RequestMethod.POST)
    public ResponseResult<List<PeopleVO>> searchCommunityImportantPeople(@RequestBody PeopleDTO param) {
        if (param == null) {
            log.error("Start search community important people, but param is null");
            return ResponseResult.error(RestErrorCode.ILLEGAL_ARGUMENT, "查询参数不能为空,请检查！");
        }
        if (param.getCommunityId() == null) {
            log.error("Start search community important people, but community id is null");
            return ResponseResult.error(RestErrorCode.ILLEGAL_ARGUMENT, "小区ID不能为空,请检查！");
        }
        if (param.getSearchVal() == null) {
            log.error("Start search community important people, but searchVal is error");
            return ResponseResult.error(RestErrorCode.ILLEGAL_ARGUMENT, "查询内容不能为NULL,请检查!");
        }
        if (param.getStart() < 0) {
            log.error("Start search community important people, but start < 0");
            return ResponseResult.error(RestErrorCode.ILLEGAL_ARGUMENT, "起始行数不能小于0,请检查！");
        }
        if (param.getLimit() <= 0) {
            log.error("Start search community important people, but limit <= 0");
            return ResponseResult.error(RestErrorCode.ILLEGAL_ARGUMENT, "分页行数不能小于或等于0,请检查！");
        }
        log.info("Start search community important people, param is:" + JacksonUtil.toJson(param));
        List<PeopleVO> voList = peopleCountService.searchCommunityImportantPeople(param);
        log.info("Search community important people successfully");
        return ResponseResult.init(voList);
    }

    @ApiOperation(value = "小区关爱人口查询", response = PeopleVO.class)
    @RequestMapping(value = BigDataPath.COMMUNITY_PEOPLE_CARE, method = RequestMethod.POST)
    public ResponseResult<List<PeopleVO>> searchCommunityCarePeople(@RequestBody PeopleDTO param) {
        if (param == null) {
            log.error("Start search community care people, but param is null");
            return ResponseResult.error(RestErrorCode.ILLEGAL_ARGUMENT, "查询参数不能为空,请检查！");
        }
        if (param.getCommunityId() == null || param.getCommunityId() == 0) {
            log.error("Start search community care people, but community id is null");
            return ResponseResult.error(RestErrorCode.ILLEGAL_ARGUMENT, "小区ID不能为空,请检查！");
        }
        if (param.getStart() < 0) {
            log.error("Start search community care people, but start < 0");
            return ResponseResult.error(RestErrorCode.ILLEGAL_ARGUMENT, "起始行数不能小于0,请检查！");
        }
        if (param.getLimit() <= 0) {
            log.error("Start search community care people, but limit <= 0");
            return ResponseResult.error(RestErrorCode.ILLEGAL_ARGUMENT, "分页行数不能小于或等于0,请检查！");
        }
        log.info("Start search community care people, param is:" + JacksonUtil.toJson(param));
        List<PeopleVO> voList = peopleCountService.searchCommunityCarePeople(param);
        log.info("Search community care people successfully");
        return ResponseResult.init(voList);
    }

    @ApiOperation(value = "小区新增人口查询（上月确认迁入数量）", response = PeopleVO.class)
    @RequestMapping(value = BigDataPath.COMMUNITY_PEOPLE_NEW, method = RequestMethod.POST)
    public ResponseResult<List<PeopleVO>> searchCommunityNewPeople(@RequestBody PeopleDTO param) {
        System.out.println(JacksonUtil.toJson(param));
        if (param == null) {
            log.error("Start search community new people, but param is null");
            return ResponseResult.error(RestErrorCode.ILLEGAL_ARGUMENT, "查询参数不能为空,请检查！");
        }
        if (param.getCommunityId() == null) {
            log.error("Start search community new people, but community id is null");
            return ResponseResult.error(RestErrorCode.ILLEGAL_ARGUMENT, "小区ID不能为空,请检查！");
        }
        if (param.getStart() < 0) {
            log.error("Start search community new people, but start < 0");
            return ResponseResult.error(RestErrorCode.ILLEGAL_ARGUMENT, "起始行数不能小于0,请检查！");
        }
        if (param.getLimit() <= 0) {
            log.error("Start search community new people, but limit <= 0");
            return ResponseResult.error(RestErrorCode.ILLEGAL_ARGUMENT, "分页行数不能小于或等于0,请检查！");
        }
        log.info("Start search community new people, param is:" + JacksonUtil.toJson(param));
        List<PeopleVO> voList = peopleCountService.searchCommunityNewPeople(param);
        log.info("Search community new people successfully");
        return ResponseResult.init(voList);
    }

    @ApiOperation(value = "小区迁出人口查询（上月确认迁出数量）", response = PeopleVO.class)
    @RequestMapping(value = BigDataPath.COMMUNITY_PEOPLE_OUT, method = RequestMethod.POST)
    public ResponseResult<List<PeopleVO>> searchCommunityOutPeople(@RequestBody PeopleDTO param) {
        if (param == null) {
            log.error("Start search community out people, but param is null");
            return ResponseResult.error(RestErrorCode.ILLEGAL_ARGUMENT, "查询参数不能为空,请检查！");
        }
        if (param.getCommunityId() == null) {
            log.error("Start search community out people, but community id is null");
            return ResponseResult.error(RestErrorCode.ILLEGAL_ARGUMENT, "小区ID不能为空,请检查！");
        }
        if (param.getStart() < 0) {
            log.error("Start search community out people, but start < 0");
            return ResponseResult.error(RestErrorCode.ILLEGAL_ARGUMENT, "起始行数不能小于0,请检查！");
        }
        if (param.getLimit() <= 0) {
            log.error("Start search community out people, but limit <= 0");
            return ResponseResult.error(RestErrorCode.ILLEGAL_ARGUMENT, "分页行数不能小于或等于0,请检查！");
        }
        log.info("Start search community out people, param is:" + JacksonUtil.toJson(param));
        List<PeopleVO> voList = peopleCountService.searchCommunityOutPeople(param);
        log.info("Search community out people successfully");
        return ResponseResult.init(voList);
    }
}
