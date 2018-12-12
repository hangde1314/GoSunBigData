package com.hzgc.service.people.controller;

import com.hzgc.common.service.error.RestErrorCode;
import com.hzgc.common.service.response.ResponseResult;
import com.hzgc.common.service.rest.BigDataPath;
import com.hzgc.common.util.json.JacksonUtil;
import com.hzgc.service.people.model.Imei;
import com.hzgc.service.people.param.SearchPeopleDTO;
import com.hzgc.service.people.param.*;
import com.hzgc.service.people.service.PeopleService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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

    /**
     * 添加人口
     *
     * @param peopleDTO 人口信息
     * @return 成功状态 1：插入成功, 0：插入失败
     */
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

    /**
     * 删除人口库信息
     *
     * @param peopleDTO 人员id
     * @return 成功状态 1:删除成功, 0:删除失败
     */
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

    /**
     * 修改人口信息
     *
     * @param peopleDTO 人口信息
     * @return 成功状态 1：修改成功, 0：修改失败
     */
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

    /**
     * 添加精神病人手环信息
     *
     * @param pram 精神病人信息
     * @return 成功状态 1：插入成功, 0：插入失败
     */
    @ApiOperation(value = "添加精神病人手环信息", response = ResponseResult.class)
    @RequestMapping(value = BigDataPath.MENTALPATIENT_INSERT, method = RequestMethod.POST)
    public ResponseResult<Integer> insertMentalPatient(@RequestBody @ApiParam(name = "入参", value = "精神病人手环信息") MentalPatientDTO pram) {
        if (pram == null) {
            log.error("Start insert mentalPatient info, but pram is null");
            return ResponseResult.error(RestErrorCode.ILLEGAL_ARGUMENT, "添加信息为空，请检查！");
        }
        if (StringUtils.isBlank(pram.getGuardianName())) {
            log.error("Start insert mentalPatient info, but guardianName is null");
            return ResponseResult.error(RestErrorCode.ILLEGAL_ARGUMENT, "添加监护人姓名为空，请检查！");
        }
        if (StringUtils.isBlank(pram.getImei())) {
            log.error("Start insert mentalPatient info, but Imei is null");
            return ResponseResult.error(RestErrorCode.ILLEGAL_ARGUMENT, "添加Imei为空，请检查！");
        }
        log.info("Start insert mentalPatient info, param DTO:" + JacksonUtil.toJson(pram));
        Integer status = peopleService.insertMentalPatient(pram);
        if (status != 1){
            log.error("Insert mentalPatient info failed");
            return ResponseResult.init(0);
        }
        log.info("Insert mentalPatient info successfully");
        return ResponseResult.init(status);
    }

    /**
     * 修改精神病人手环信息
     *
     * @param param 精神病人信息
     * @return 成功状态 1：插入成功, 0：插入失败
     */
    @ApiOperation(value = "修改精神病人手环信息", response = ResponseResult.class)
    @RequestMapping(value = BigDataPath.MENTALPATIENT_UPDATE, method = RequestMethod.PUT)
    public ResponseResult<Integer> updateMentalPatient(@RequestBody @ApiParam(name = "入参", value = "精神病人手环信息") MentalPatientDTO param) {
        if (param == null) {
            log.error("Start update mentalPatient info, but param is null");
            return ResponseResult.error(RestErrorCode.ILLEGAL_ARGUMENT, "修改信息为空，请检查！");
        }
        if (param.getId() == null) {
            log.error("Start update mentalPatient info, but imei id is null");
            return ResponseResult.error(RestErrorCode.ILLEGAL_ARGUMENT, "修改ID为空，请检查！");
        }
        log.info("Start update mentalPatient info, param DTO:" + JacksonUtil.toJson(param));
        Integer status = peopleService.updateMentalPatient(param);
        if (status != 1){
            log.error("Update mentalPatient info failed");
            ResponseResult.error(RestErrorCode.ILLEGAL_ARGUMENT, "修改精神病人手环信息失败");
        }
        return ResponseResult.init(1);
    }

    /**
     * 根据ID查询精神病人手环信息
     *
     * @param peopleId 人员全局ID
     * @return Imei
     */
    @ApiOperation(value = "根据ID查询精神病人手环信息", response = ImeiVO.class)
    @RequestMapping(value = BigDataPath.MENTALPATIENT_SELECT_BY_PEOPLEID, method = RequestMethod.GET)
    public ResponseResult<ImeiVO> selectMentalPatientByPeopleId(String peopleId) {
        if (StringUtils.isBlank(peopleId)) {
            log.error("Start select mentalPatient info, but people id is null");
            return ResponseResult.error(RestErrorCode.ILLEGAL_ARGUMENT, "查询ID为空，请检查！");
        }
        log.info("Start select mentalPatient info, people id is:" + peopleId);
        ImeiVO vo = peopleService.selectMentalPatientByPeopleId(peopleId);
        log.info("Select mentalPatient info successfully, result:" + JacksonUtil.toJson(vo));
        return ResponseResult.init(vo);
    }

    /**
     * 根据ID查询人口信息
     *
     * @param peopleId 人员全局ID
     * @return PeopleVO
     */
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

    /**
     * 根据精神病手环ID(IMEI)查询人口信息
     *
     * @param imeiId 精神病手环ID
     * @return PeopleVO
     */
    @ApiOperation(value = "根据精神病手环ID(IMEI)查询人口信息", response = PeopleVO.class)
    @RequestMapping(value = BigDataPath.PEOPLE_SELECT_BY_IMEIID, method = RequestMethod.GET)
    public ResponseResult<PeopleVO> selectByImeiId(String imeiId) {
        if (StringUtils.isBlank(imeiId)) {
            log.error("Start select people info, but IMEI ID is null");
            return ResponseResult.error(RestErrorCode.ILLEGAL_ARGUMENT, "查询手环ID为空，请检查！");
        }
        log.info("Start select people info, IMEI ID is:" + imeiId);
        PeopleVO peopleVO = peopleService.selectByImeiId(imeiId);
        log.info("Select people info successfully, result:" + JacksonUtil.toJson(peopleVO));
        return ResponseResult.init(peopleVO);
    }

    /**
     * 根据身份证查询人员信息
     *
     * @param idCard 身份证
     * @return PeopleVO 人员信息
     */
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

    /**
     * 添加人口库照片信息
     *
     * @param pictureDTO 添加照片信息
     * @return 成功状态 1：修改成功, 0：修改失败
     */
    @ApiOperation(value = "添加人口库照片信息", response = byte[].class)
    @RequestMapping(value = BigDataPath.PEOPLE_INSTERT_PICTURE, method = RequestMethod.POST)
    public ResponseResult<Integer> insertPicture(@RequestBody PictureDTO pictureDTO) {
        if (pictureDTO == null) {
            log.error("Start insert picture, but param is null");
            return ResponseResult.error(RestErrorCode.ILLEGAL_ARGUMENT, "添加信息为空，请检查！");
        }
        if (pictureDTO.getType() != 0 && pictureDTO.getType() != 1) {
            log.error("Start insert picture, but type is error");
            return ResponseResult.error(RestErrorCode.ILLEGAL_ARGUMENT, "类型选择有误，请检查！");
        }
        if (StringUtils.isBlank(pictureDTO.getPeopleId())) {
            log.error("Start insert picture, but people id is null");
            return ResponseResult.error(RestErrorCode.ILLEGAL_ARGUMENT, "人口ID为空，请检查！");
        }
        if (StringUtils.isBlank(pictureDTO.getPicture())) {
            log.error("Start insert picture, but picture data id is null");
            return ResponseResult.error(RestErrorCode.ILLEGAL_ARGUMENT, "照片数据为空，请检查！");
        }
        log.info("Start insert picture, param is:" + JacksonUtil.toJson(pictureDTO));
        int status = peopleService.insertPicture(pictureDTO);
        return ResponseResult.init(status);
    }

    /**
     * 修改人口库照片信息
     *
     * @param pictureDTO 修改照片信息
     * @return 成功状态 1：修改成功, 0：修改失败
     */
    @ApiOperation(value = "修改人口库照片信息", response = byte[].class)
    @RequestMapping(value = BigDataPath.PEOPLE_UPDATE_PICTURE, method = RequestMethod.PUT)
    public ResponseResult<Integer> updatePicture(@RequestBody PictureDTO pictureDTO) {
        if (pictureDTO == null) {
            log.error("Start update picture, but param is null");
            return ResponseResult.error(RestErrorCode.ILLEGAL_ARGUMENT, "修改信息为空，请检查！");
        }
        if (pictureDTO.getType() != 0 && pictureDTO.getType() != 1) {
            log.error("Start update picture, but type is error");
            return ResponseResult.error(RestErrorCode.ILLEGAL_ARGUMENT, "类型选择有误，请检查！");
        }
        if (pictureDTO.getPictureId() == null) {
            log.error("Start update picture, but picture id is null");
            return ResponseResult.error(RestErrorCode.ILLEGAL_ARGUMENT, "照片ID为空，请检查！");
        }
        if (StringUtils.isBlank(pictureDTO.getPeopleId())) {
            log.error("Start update picture, but people id is null");
            return ResponseResult.error(RestErrorCode.ILLEGAL_ARGUMENT, "人口ID为空，请检查！");
        }
        if (StringUtils.isBlank(pictureDTO.getPicture())) {
            log.error("Start update picture, but picture data id is null");
            return ResponseResult.error(RestErrorCode.ILLEGAL_ARGUMENT, "照片数据为空，请检查！");
        }
        log.info("Start update picture, param is:" + JacksonUtil.toJson(pictureDTO));
        int status = peopleService.updatePicture(pictureDTO);
        return ResponseResult.init(status);
    }

    /**
     * 删除人口库照片信息
     *
     * @param pictureDTO 删除照片信息
     * @return 成功状态 1：修改成功, 0：修改失败
     */
    @ApiOperation(value = "删除人口库照片信息", response = byte[].class)
    @RequestMapping(value = BigDataPath.PEOPLE_DELETE_PICTURE, method = RequestMethod.DELETE)
    public ResponseResult<Integer> deletePicture(@RequestBody PictureDTO pictureDTO) {
        if (pictureDTO == null) {
            log.error("Start delete picture, but param is null");
            return ResponseResult.error(RestErrorCode.ILLEGAL_ARGUMENT, "删除信息为空，请检查！");
        }
        if (StringUtils.isBlank(pictureDTO.getPeopleId())) {
            log.error("Start delete picture, but people id is null");
            return ResponseResult.error(RestErrorCode.ILLEGAL_ARGUMENT, "人口ID为空，请检查！");
        }
        if (pictureDTO.getPictureId() == null) {
            log.error("Start delete picture, but picture id is null");
            return ResponseResult.error(RestErrorCode.ILLEGAL_ARGUMENT, "照片ID为空，请检查！");
        }
        log.info("Start delete picture, param is:" + JacksonUtil.toJson(pictureDTO));
        int status = peopleService.deletePicture(pictureDTO);
        return ResponseResult.init(status);
    }

    /**
     * 根据照片ID查询照片
     *
     * @param pictureId 照片ID
     * @return byte[] 照片
     */
    @ApiOperation(value = "根据照片ID查询照片", response = byte[].class)
    @RequestMapping(value = BigDataPath.PEOPLE_SEARCH_PICTURE_BY_PICID, method = RequestMethod.GET)
    public ResponseEntity<byte[]> searchPictureByPicId(Long pictureId) {
        if (pictureId == null) {
            log.error("Start select picture, but picture id is null");
            ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(null);
        }
        log.debug("Start select picture, picture id is:" + pictureId);
        byte[] picture = peopleService.searchPictureByPicId(pictureId);
        if (picture == null || picture.length == 0) {
            return ResponseEntity.badRequest().contentType(MediaType.IMAGE_JPEG).body(null);
        }
        return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(picture);
    }

    /**
     * 根据人员全局ID查询照片
     *
     * @param peopleId 人员全局ID
     * @return PictureVO 照片封装
     */
    @ApiOperation(value = "根据人员全局ID查询照片", response = PictureVO.class)
    @RequestMapping(value = BigDataPath.PEOPLE_SEARCH_PICTURE_BY_PEOPLEID, method = RequestMethod.GET)
    public ResponseResult<PictureVO> searchPictureByPeopleId(String peopleId) {
        if (StringUtils.isBlank(peopleId)) {
            log.error("Start select picture, but people id is null");
            return ResponseResult.error(RestErrorCode.ILLEGAL_ARGUMENT, "查询ID为空，请检查！");
        }
        log.info("Start select picture, people id is:" + peopleId);
        PictureVO pictureVO = peopleService.searchPictureByPeopleId(peopleId);
        log.info("Slect picture successfully");
        return ResponseResult.init(pictureVO);
    }

    /**
     * 根据条件查询人员
     *
     * @param param 查询条件参数封装
     * @return peopleVO 查询返回参数封装
     */
    @ApiOperation(value = "根据条件查询人员", response = SearchPeopleVO.class)
    @RequestMapping(value = BigDataPath.PEOPLE_SELECT_PEOPLE, method = RequestMethod.POST)
    public ResponseResult<SearchPeopleVO> searchPeople(@RequestBody @ApiParam(value = "查询条件") SearchPeopleDTO param) {
        if (param == null) {
            log.error("Start search people, but param is null");
            return ResponseResult.error(RestErrorCode.ILLEGAL_ARGUMENT, "查询参数为空,请检查!");
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

    @ApiOperation(value = "统计单个范围（省市区）下所有实有小区列表", response = List.class)
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

    /**
     * excel表格导入
     *
     * @param file 文件路径
     * @return 状态 1 ：修改成功 0 ：修改失败
     */
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

    /**
     * excel表格模板下载
     */
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
