package com.hzgc.cloud.people.controller;

import com.hzgc.cloud.people.param.PeopleDTO;
import com.hzgc.cloud.people.param.PeopleVO;
import com.hzgc.cloud.people.param.SearchPeopleDTO;
import com.hzgc.cloud.people.param.SearchPeopleVO;
import com.hzgc.cloud.people.service.PeopleService;
import com.hzgc.common.service.error.RestErrorCode;
import com.hzgc.common.service.response.ResponseResult;
import com.hzgc.common.service.rest.BigDataPath;
import com.hzgc.common.util.json.JacksonUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@Api(value = "/people", tags = "人口库服务")
@Slf4j
public class PeopleController {
    @Autowired
    @SuppressWarnings("unused")
    private PeopleService peopleService;

    @ApiOperation(value = "添加人口信息", response = ResponseResult.class)
    @RequestMapping(value = BigDataPath.PEOPLE_INSERT, method = RequestMethod.POST)
    public ResponseResult<String> insertPeople(@RequestBody PeopleDTO peopleDTO) {
        if (peopleDTO == null) {
            log.error("Start insert people info, but people is null");
            return ResponseResult.error(RestErrorCode.ILLEGAL_ARGUMENT, "添加信息为空，请检查！");
        }
        if (StringUtils.isBlank(peopleDTO.getName())) {
            log.error("Start insert people info, but name is null");
            return ResponseResult.error(RestErrorCode.ILLEGAL_ARGUMENT, "添加姓名为空，请检查！");
        }
        if (StringUtils.isBlank(peopleDTO.getIdCard())) {
            log.error("Start insert people info, but idCard is null");
            return ResponseResult.error(RestErrorCode.ILLEGAL_ARGUMENT, "添加身份证为空，请检查！");
        }
        boolean boo = peopleService.CheckIdCard(peopleDTO.getIdCard());
        if (boo) {
            log.error("Start insert people info, but idCard is exist");
            return ResponseResult.error(RestErrorCode.ILLEGAL_ARGUMENT, "添加身份证已存在，请检查！");
        }
        if (peopleDTO.getRegion() == null) {
            log.error("Start insert people info, but region is null");
            return ResponseResult.error(RestErrorCode.ILLEGAL_ARGUMENT, "添加区域为空，请检查！");
        }
        log.info("Start insert people info, param DTO:" + JacksonUtil.toJson(peopleDTO));
        String id = peopleService.insertPeople(peopleDTO);
        return ResponseResult.init(id);
    }

    @ApiOperation(value = "删除人口库信息", response = Integer.class)
    @RequestMapping(value = BigDataPath.PEOPLE_DELETE, method = RequestMethod.DELETE)
    public ResponseResult<Integer> deletePeople(@RequestBody PeopleDTO peopleDTO) {
        if (peopleDTO == null) {
            log.error("Start delete people info, but people is null");
            return ResponseResult.error(RestErrorCode.ILLEGAL_ARGUMENT, "删除信息为空，请检查！");
        }
        if (StringUtils.isBlank(peopleDTO.getId())) {
            log.error("Start delete people info,but id is null");
            return ResponseResult.error(RestErrorCode.ILLEGAL_ARGUMENT, "删除id为空,请检查!");
        }
        log.info("Start delete people info, id is:" + peopleDTO.getId());
        Integer status = peopleService.deletePeople(peopleDTO.getId());
        if (status == 1) {
            log.info("Delete people info successfully");
        }
        return ResponseResult.init(status);
    }

    @ApiOperation(value = "修改人口信息", response = ResponseResult.class)
    @RequestMapping(value = BigDataPath.PEOPLE_UPDATE, method = RequestMethod.PUT)
    public ResponseResult<Integer> updatePeople(@RequestBody PeopleDTO peopleDTO) {
        if (peopleDTO == null) {
            log.error("Start update people info, but people is null");
            return ResponseResult.error(RestErrorCode.ILLEGAL_ARGUMENT, "修改信息为空，请检查！");
        }
        if (peopleDTO.getId() == null) {
            log.error("Start update people info, but people id is null");
            return ResponseResult.error(RestErrorCode.ILLEGAL_ARGUMENT, "修改ID为空，请检查！");
        }
        if (peopleDTO.getRegion() == null) {
            log.error("Start update people info, but region is null");
            return ResponseResult.error(RestErrorCode.ILLEGAL_ARGUMENT, "修改区域为空，请检查！");
        }
        log.info("Start update people info, param DTO:" + JacksonUtil.toJson(peopleDTO));
        Integer status = peopleService.updatePeople(peopleDTO);
        return ResponseResult.init(status);
    }

    @ApiOperation(value = "根据ID查询人口信息", response = PeopleVO.class)
    @RequestMapping(value = BigDataPath.PEOPLE_SELECT_BY_PEOPLEID, method = RequestMethod.GET)
    public ResponseResult<PeopleVO> selectByPeopleId(String peopleId) {
        if (StringUtils.isBlank(peopleId)) {
            log.error("Start select people info, but people id is null");
            return ResponseResult.error(RestErrorCode.ILLEGAL_ARGUMENT, "查询ID为空，请检查！");
        }
        log.info("Start select people info, people id is:" + peopleId);
        PeopleVO peopleVO = peopleService.selectByPeopleId(peopleId);
        log.info("Select people info successfully, result:" + JacksonUtil.toJson(peopleVO));
        return ResponseResult.init(peopleVO);
    }

    @ApiOperation(value = "根据身份证查询人员信息", response = PeopleVO.class)
    @RequestMapping(value = BigDataPath.PEOPLE_SEARCH_BY_IDCARD, method = RequestMethod.GET)
    public PeopleVO searchPeopleByIdCard(@RequestParam @ApiParam(value = "身份证", required = true) String idCard) {
        if (StringUtils.isBlank(idCard)) {
            log.error("Start select people info, but idCard is null");
            return null;
        }
        log.info("Start select people info, idCard is:" + idCard);
        return peopleService.searchPeopleByIdCard(idCard);
    }

    @ApiOperation(value = "根据条件查询人员", response = SearchPeopleVO.class)
    @RequestMapping(value = BigDataPath.PEOPLE_SELECT_PEOPLE, method = RequestMethod.POST)
    public ResponseResult<SearchPeopleVO> searchPeople(@RequestBody @ApiParam(value = "查询条件") SearchPeopleDTO param) {
        if (param == null) {
            log.error("Start search people, but param is null");
            return ResponseResult.error(RestErrorCode.ILLEGAL_ARGUMENT, "查询参数为空,请检查!");
        }
        if (param.getRegionIds() != null && param.getRegionIds().size() > 0 &&
                param.getGridCodes() != null && param.getGridCodes().size() > 0) {
            log.error("Start search people, but region and grid is not null");
            return ResponseResult.error(RestErrorCode.ILLEGAL_ARGUMENT, "不支持区域和网格合并查询,请检查!");
        }
        if (param.getSearchType() != 0 && param.getSearchType() != 1 && param.getSearchType() != 2 &&
                param.getSearchType() != 3 && param.getSearchType() != 4 && param.getSearchType() != 5){
            log.error("Start search people, but SearchType is error");
            return ResponseResult.error(RestErrorCode.ILLEGAL_ARGUMENT, "查询类型不正确,请检查!");
        }
        if (param.getSearchVal() == null) {
            log.error("Start search people, but searchVal is error");
            return ResponseResult.error(RestErrorCode.ILLEGAL_ARGUMENT, "查询内容不能为NULL,请检查!");
        }
        if (param.getStart() < 0) {
            log.error("Start search people, but start < 0");
            return ResponseResult.error(RestErrorCode.ILLEGAL_ARGUMENT, "起始行数不能小于0,请检查！");
        }
        if (param.getLimit() <= 0) {
            log.error("Start search people, but limit <= 0");
            return ResponseResult.error(RestErrorCode.ILLEGAL_ARGUMENT, "分页行数不能小于或等于0,请检查！");
        }
        log.info("Start search people, search param DTO:" + JacksonUtil.toJson(param));
        SearchPeopleVO vo = peopleService.searchPeople(param);
        log.info("Search people successfully, result:" + JacksonUtil.toJson(vo));
        return ResponseResult.init(vo, vo != null ? vo.getTotal() : 0);
    }

    @ApiOperation(value = "统计单个区域（省市区）下所有实有小区列表", response = List.class)
    @RequestMapping(value = BigDataPath.PEOPLE_SELECT_COMMUNITY, method = RequestMethod.GET)
    public ResponseResult<List<Long>> searchCommunityIdsById(Long regionId) {
        if (regionId == null) {
            log.info("Start search community id list, but id is null");
            ResponseResult.error(RestErrorCode.ILLEGAL_ARGUMENT, "查询范围为空,请检查!");
        }
        log.info("Start search community id list, id is:" + regionId);
        List<Long> communityIds = peopleService.searchCommunityIdsById(regionId);
        log.info("Search community id list successfully, result:" + JacksonUtil.toJson(communityIds));
        return ResponseResult.init(communityIds);
    }

    @ApiOperation(value = "人口库excel表格导入")
    @RequestMapping(value = BigDataPath.PEOPLE_EXCEL_IMPORT, method = RequestMethod.POST)
    public ResponseResult<Integer> excelImport(MultipartFile file) {
        if (file == null) {
            log.error("Start import excel data, but file is null");
            return ResponseResult.error(RestErrorCode.ILLEGAL_ARGUMENT, "导入表格为空，请检查！");
        }
        log.info("Start import excel data");
        Integer status = peopleService.excelImport(file);
        if (status != 1) {
            log.error("Import excel data failed");
            return ResponseResult.error(RestErrorCode.ILLEGAL_ARGUMENT, "导入表格失败！");
        }
        log.info("Import excel data successfully");
        return ResponseResult.init(1);
    }

    @GetMapping("/excel")
    @ApiOperation("下载模板")
    public ResponseEntity<byte[]> downloadExcel() {
        ClassPathResource cpr = new ClassPathResource("excel/people_excel.xlsx");
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentDispositionFormData("people", "people_excel.xlsx");
            byte[] bytes = FileCopyUtils.copyToByteArray(cpr.getInputStream());
            return new ResponseEntity<>(bytes, headers, HttpStatus.OK);
        } catch (IOException e) {
            throw new RuntimeException("读取模板文件失败");
        }
    }
}
