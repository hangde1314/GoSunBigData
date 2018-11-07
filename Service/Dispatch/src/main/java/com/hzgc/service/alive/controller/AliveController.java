package com.hzgc.service.alive.controller;

import com.hzgc.common.service.error.RestErrorCode;
import com.hzgc.common.service.response.ResponseResult;
import com.hzgc.common.service.rest.BigDataPath;
import com.hzgc.common.util.json.JacksonUtil;
import com.hzgc.service.alive.param.AliveIdObject;
import com.hzgc.service.alive.param.AliveInfoDTO;
import com.hzgc.service.alive.param.AliveInfoVO;
import com.hzgc.service.alive.param.SearchAliveInfoDTO;
import com.hzgc.service.alive.service.AliveService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@Api(value = "/alive", tags = "活体检测服务")
public class AliveController {
    @Autowired
    private AliveService aliveService;

    @ApiOperation(value = "添加活体名单信息", response = Integer.class)
    @RequestMapping(value = BigDataPath.DISPATCH_INSERT_ALIVE, method = RequestMethod.POST)
    public ResponseResult<Integer> insertAliveInfo(@RequestBody @ApiParam(name = "入参", value = "布控信息") AliveInfoDTO dto) {
        System.out.println(JacksonUtil.toJson(dto));
        if (dto == null) {
            log.error("Start insert alive info, but dto is null");
            return ResponseResult.error(RestErrorCode.ILLEGAL_ARGUMENT, "添加白名单信息为空,请检查!");
        }
        if (dto.getDeviceIds() == null || dto.getDeviceIds().size() == 0){
            log.error("Start insert alive info, but device id list is null");
            return ResponseResult.error(RestErrorCode.ILLEGAL_ARGUMENT, "相机列表为空,请检查!");
        }
        if (StringUtils.isBlank(dto.getStarttime())){
            log.error("Start insert alive info, but starttime is null");
            return ResponseResult.error(RestErrorCode.ILLEGAL_ARGUMENT, "起始时间为空,请检查!");
        }
        if (StringUtils.isBlank(dto.getEndtime())){
            log.error("Start insert alive info, but endtime is null");
            return ResponseResult.error(RestErrorCode.ILLEGAL_ARGUMENT, "结束时间为空,请检查!");
        }
        log.info("Start insert alive info, dto is :" + JacksonUtil.toJson(dto));
        Integer status = aliveService.insertAliveInfo(dto);
        log.info("Insert info successfully");
        return ResponseResult.init(status);
    }

    @ApiOperation(value = "修改活体名单信息", response = Integer.class)
    @RequestMapping(value = BigDataPath.DISPATCH_UPDATE_ALIVE, method = RequestMethod.POST)
    public ResponseResult<Integer> updateAliveInfo(@RequestBody @ApiParam(name = "入参", value = "布控信息") AliveInfoDTO dto) {
        if (dto == null) {
            log.error("Start update alive info,but dto is null");
            return ResponseResult.error(RestErrorCode.ILLEGAL_ARGUMENT, "修改对象为空,请检查!");
        }
        if (StringUtils.isBlank(dto.getId())) {
            log.error("Start update alive info,but id is null");
            return ResponseResult.error(RestErrorCode.ILLEGAL_ARGUMENT, "修改对象ID为空,请检查!");
        }
        log.info("Start update alive info ,dto : " + JacksonUtil.toJson(dto));
        Integer status = aliveService.updateAliveInfo(dto);
        if (status == 1) {
            log.info("Update alive info sucessfully");
            return ResponseResult.init(1);
        } else {
            log.info("Update alive info failed");
            return ResponseResult.init(0);
        }
    }

    @ApiOperation(value = "删除活体名单信息", response = Integer.class)
    @RequestMapping(value = BigDataPath.DISPATCH_DELETE_ALIVE, method = RequestMethod.DELETE)
    public ResponseResult<Integer> deleteAliveInfo(@ApiParam(value = "ID", required = true) @RequestBody AliveIdObject idObject) {
        if (idObject == null || StringUtils.isBlank(idObject.getId())) {
            log.error("Start delete alive info,but id is null");
            return ResponseResult.error(RestErrorCode.ILLEGAL_ARGUMENT, "删除id为空,请检查");
        }
        log.info("Start delete alive info,,id is " + idObject.getId());
        Integer status = aliveService.deleteAliveInfo(idObject.getId());
        if (status == 1) {
            log.info("Delete alive info  successfully");
            return ResponseResult.init(1);
        } else {
            log.info("Delete alive info failed");
            return ResponseResult.init(0);
        }
    }

    @ApiOperation(value = "开启/停止活体名单布控", response = Integer.class)
    @RequestMapping(value = BigDataPath.DISPATCH_ALIVE_STATUS, method = RequestMethod.GET)
    public ResponseResult<Integer> updateAliveInfoStatus(
            @ApiParam(value = "布控ID", required = true) @RequestParam String id,
            @ApiParam(value = "人员状态(0:开启,1:停止)", required = true) @RequestParam int status) {
        if (StringUtils.isBlank(id)) {
            log.error("Start update dispatch status, but id is error");
            return ResponseResult.error(RestErrorCode.ILLEGAL_ARGUMENT, "人员ID为空，请检查");
        }
        if (status != 0 && status != 1) {
            log.error("Start update dispatch status, but status is error");
            return ResponseResult.error(RestErrorCode.ILLEGAL_ARGUMENT, "人员状态错误，请检查");
        }
        log.info("Start update dispatch status, id: " + id + ", status: " + status);
        Integer i = aliveService.updateAliveInfoStatus(id, status);
        if (i != 1) {
            return ResponseResult.init(0);
        }
        log.info("Update dispatch status successfully");
        return ResponseResult.init(1);
    }

    @ApiOperation(value = "查询活体信息（模糊查询）", response = AliveInfoVO.class)
    @RequestMapping(value = BigDataPath.DISPATCH_SEARCH_ALIVE, method = RequestMethod.POST)
    public ResponseResult<AliveInfoVO> searchAliveInfo(@RequestBody SearchAliveInfoDTO dto) {
        if (dto == null) {
            log.error("Start search alive info , but dto is null");
            return ResponseResult.error(RestErrorCode.ILLEGAL_ARGUMENT, "查询参数为空，请检查");
        }
        if (dto.getStart() < 0) {
            log.error("Start search alive, but start < 0");
            return ResponseResult.error(RestErrorCode.ILLEGAL_ARGUMENT, "起始行数不能小于0,请检查！");
        }
        if (dto.getLimit() <= 0) {
            log.error("Start search alive, but limit <= 0");
            return ResponseResult.error(RestErrorCode.ILLEGAL_ARGUMENT, "分页行数不能小于或等于0,请检查！");
        }
        log.info("Start search dispatch, search dispatch:" + JacksonUtil.toJson(dto));
        AliveInfoVO vo = aliveService.searchAliveInfo(dto);
        log.info("Search search dispatch alive successfully, result:" + JacksonUtil.toJson(vo));
        return ResponseResult.init(vo, vo != null ? vo.getTotal() : 0);
    }
}
