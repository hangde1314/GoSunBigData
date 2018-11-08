package com.hzgc.service.white.controller;

import com.hzgc.common.service.error.RestErrorCode;
import com.hzgc.common.service.response.ResponseResult;
import com.hzgc.common.service.rest.BigDataPath;
import com.hzgc.common.util.json.JacksonUtil;
import com.hzgc.service.white.param.SearchWhiteDTO;
import com.hzgc.service.white.param.SearchWhiteVO;
import com.hzgc.service.white.param.WhiteDTO;
import com.hzgc.service.white.param.WhiteIdObject;
import com.hzgc.service.white.service.WhiteService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@Api(value = "/white", tags = "白名单服务")
public class WhiteController {
    @Autowired
    private WhiteService whiteService;

    @ApiOperation(value = "添加白名单信息", response = Integer.class)
    @RequestMapping(value = BigDataPath.DISPATCH_INSERT_WHITE, method = RequestMethod.POST)
    public ResponseResult<Integer> insertWhiteInfo(@RequestBody @ApiParam(name = "入参", value = "布控信息") WhiteDTO dto) {
        if (dto == null) {
            log.error("Start insert white info, but param is null");
            return ResponseResult.error(RestErrorCode.ILLEGAL_ARGUMENT, "添加信息为空,请检查!");
        }
        if (StringUtils.isNotBlank(dto.getId())) {
            log.error("Start insert white info, but param is error");
            return ResponseResult.error(RestErrorCode.ILLEGAL_ARGUMENT, "添加不需要ID,请检查!");
        }
        if (StringUtils.isBlank(dto.getName())){
            log.error("Start insert white info, but name is null");
            return ResponseResult.error(RestErrorCode.ILLEGAL_ARGUMENT, "布控名称为空,请检查!");
        }
        if (dto.getDeviceIds() == null || dto.getDeviceIds().size() == 0){
            log.error("Start insert white info, but device id list is null");
            return ResponseResult.error(RestErrorCode.ILLEGAL_ARGUMENT, "相机列表为空,请检查!");
        }
        if (dto.getPeopleInfos() == null || dto.getPeopleInfos().size() == 0){
            log.info("Start insert white info, but people info list is null");
            return ResponseResult.error(RestErrorCode.ILLEGAL_ARGUMENT, "布控成员为空,请检查!");
        }
        log.info("Start insert white info, param is :" + JacksonUtil.toJson(dto));
        Integer status = whiteService.insertWhiteInfo(dto);
        if (status == 1) {
            log.info("Insert white info successfully");
            return ResponseResult.init(1);
        } else {
            log.info("Insert white info failed");
            return ResponseResult.init(0);
        }
    }

    @ApiOperation(value = "删除白名单信息", response = Integer.class)
    @RequestMapping(value = BigDataPath.DISPATCH_DELETE_WHITE, method = RequestMethod.DELETE)
    public ResponseResult<Integer> deleteWhiteInfo(@ApiParam(value = "ID", required = true) @RequestBody WhiteIdObject idObject) {
        if (idObject == null || StringUtils.isBlank(idObject.getId())) {
            log.error("Start delete white info, but id is null");
            return ResponseResult.error(RestErrorCode.ILLEGAL_ARGUMENT, "删除ID为空,请检查");
        }
        log.info("Start delete white info, id is:" + idObject.getId());
        Integer status = whiteService.deleteWhiteInfo(idObject.getId());
        if (status == 1) {
            log.info("Delete white info successfully");
            return ResponseResult.init(1);
        } else {
            log.info("Delete white info failed");
            return ResponseResult.init(0);
        }
    }

    @ApiOperation(value = "修改白名单信息", response = Integer.class)
    @RequestMapping(value = BigDataPath.DISPATCH_UPDATE_WHITE, method = RequestMethod.POST)
    public ResponseResult<Integer> updateWhiteInfo(@RequestBody @ApiParam(name = "入参", value = "人员信息") WhiteDTO dto) {
        if (dto == null) {
            log.error("Start update white info, but param is null");
            return ResponseResult.error(RestErrorCode.ILLEGAL_ARGUMENT, "修改对象为空,请检查!");
        }
        if (StringUtils.isBlank(dto.getId())) {
            log.error("Start update white info, but id is null");
            return ResponseResult.error(RestErrorCode.ILLEGAL_ARGUMENT, "修改对象ID为空,请检查!");
        }
        if (StringUtils.isBlank(dto.getName())){
            log.error("Start insert white info, but name is null");
            return ResponseResult.error(RestErrorCode.ILLEGAL_ARGUMENT, "布控名称为空,请检查!");
        }
        if (dto.getDeviceIds() == null || dto.getDeviceIds().size() == 0){
            log.error("Start insert white info, but device id list is null");
            return ResponseResult.error(RestErrorCode.ILLEGAL_ARGUMENT, "相机列表为空,请检查!");
        }
        if (dto.getPeopleInfos() == null || dto.getPeopleInfos().size() == 0){
            log.info("Start insert white info, but people info list is null");
            return ResponseResult.error(RestErrorCode.ILLEGAL_ARGUMENT, "布控成员为空,请检查!");
        }
        log.info("Start update white info , param is:" + JacksonUtil.toJson(dto));
        Integer status = whiteService.updateWhiteInfo(dto);
        if (status == 1) {
            log.info("Update white info successfully");
            return ResponseResult.init(1);
        } else {
            log.info("Update white info failed");
            return ResponseResult.init(0);
        }
    }

    @ApiOperation(value = "开启/停止白名单人员", response = Integer.class)
    @RequestMapping(value = BigDataPath.DISPATCH_WHITE_STATUS, method = RequestMethod.GET)
    public ResponseResult<Integer> updateWhiteStatus(
            @ApiParam(value = "人员ID", required = true) @RequestParam String id,
            @ApiParam(value = "人员状态(0:开启,1:停止)", required = true) @RequestParam int status) {
        if (StringUtils.isBlank(id)) {
            log.error("Start update white status, but id is null");
            return ResponseResult.error(RestErrorCode.ILLEGAL_ARGUMENT, "布控ID为空，请检查!");
        }
        if (status != 0 && status != 1) {
            log.error("Start update white status, but status is error");
            return ResponseResult.error(RestErrorCode.ILLEGAL_ARGUMENT, "布控状态错误，请检查!");
        }
        log.info("Start update white status, id:" + id + ", status:" + status);
        Integer i = whiteService.updateWhiteStatus(id, status);
        if (i == 1) {
            log.info("Update white status successfully");
            return ResponseResult.init(1);
        } else {
            log.info("Update white status failed");
            return ResponseResult.init(0);
        }
    }

    /**
     *
     */
    @ApiOperation(value = "查询人员白名单信息（模糊查询）", response = SearchWhiteVO.class)
    @RequestMapping(value = BigDataPath.DISPATCH_SEARCH_WHITE, method = RequestMethod.POST)
    public ResponseResult<SearchWhiteVO> searchWhiteInfo(@RequestBody SearchWhiteDTO dto) {
        if (dto == null) {
            log.error("Start search white info , but dto is null");
            return ResponseResult.error(RestErrorCode.ILLEGAL_ARGUMENT, "查询参数为空，请检查!");
        }
        if (dto.getStart() < 0) {
            log.error("Start search info, but start < 0");
            return ResponseResult.error(RestErrorCode.ILLEGAL_ARGUMENT, "起始行数不能小于0,请检查！");
        }
        if (dto.getLimit() <= 0) {
            log.error("Start search info, but limit <= 0");
            return ResponseResult.error(RestErrorCode.ILLEGAL_ARGUMENT, "分页行数不能小于或等于0,请检查！");
        }
        log.info("Start search dispatch, search dispatch:" + JacksonUtil.toJson(dto));
        SearchWhiteVO vo = whiteService.searchWhiteInfo(dto);
        log.info("Search search dispatch successfully");
        return ResponseResult.init(vo, vo != null ? vo.getTotal() : 0);
    }

    /**
     * @param id （布控人员ID）
     */
    @ApiOperation(value = "根据白名单人员ID获取布控人照片", response = byte[].class)
    @RequestMapping(value = BigDataPath.DISPATCH_GET_PICTURE, method = RequestMethod.GET)
    public ResponseEntity <byte[]> getPicture(@ApiParam(value = "人员ID", required = true) @RequestParam Long id) {
        if (id < 0) {
            log.error("Start get face, but id is error");
            ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body("查询ID参数错误，请检查");
        }
        log.info("Start get face, search dispatch:" + id);
        byte[] picByte = whiteService.getPicture(id);
        if (picByte == null || picByte.length == 0) {
            ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(null);
        }
        log.info("Start get face successfully");
        return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(picByte);
    }



}
