package com.hzgc.service.starepo.controller;

import com.hzgc.common.table.seachres.SearchResultTable;
import com.hzgc.common.util.empty.IsEmpty;
import com.hzgc.common.util.json.JSONUtil;
import com.hzgc.jni.PictureData;
import com.hzgc.service.starepo.bean.export.ObjectInfo;
import com.hzgc.service.starepo.bean.export.ObjectSearchResult;
import com.hzgc.service.starepo.bean.param.GetObjectInfoParam;
import com.hzgc.service.starepo.bean.param.ObjectInfoParam;
import com.hzgc.service.starepo.bean.param.SearchRecordParam;
import com.hzgc.service.starepo.service.ObjectInfoHandlerService;
import com.hzgc.service.util.bean.PeopleManagerCount;
import com.hzgc.service.util.error.RestErrorCode;
import com.hzgc.service.util.response.ResponseResult;
import com.hzgc.service.util.rest.BigDataPath;
import com.hzgc.service.util.rest.BigDataPermission;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.UnsupportedEncodingException;
import java.util.List;

@Slf4j
@RestController
@Api(tags = "静态库服务")
public class ObjectInfoHandlerController {

    @Autowired
    @SuppressWarnings("unused")
    private ObjectInfoHandlerService objectInfoHandlerService;

    /**
     * 添加对象
     *
     * @param param 对象信息
     * @return 成功状态【0：插入成功；1：插入失败】
     */
    @ApiOperation(value = "添加对象", response = ResponseResult.class)
    @RequestMapping(value = BigDataPath.OBJECTINFO_ADD, method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    @PreAuthorize("hasAuthority('" + BigDataPermission.OBJECT_OPERATION + "')")
    public ResponseResult<Integer> addObjectInfo(@RequestBody @ApiParam(value = "添加对象") ObjectInfoParam param) {
        if (param == null) {
            log.error("Start add object info, but param is null");
            return ResponseResult.error(RestErrorCode.ILLEGAL_ARGUMENT, "添加对象信息为空，请检查！");
        }
        if (StringUtils.isBlank(param.getObjectTypeKey())) {
            log.error("Start add object info ,but object type key is null");
            return ResponseResult.error(RestErrorCode.ILLEGAL_ARGUMENT, "添加对象类型为空，请检查！");
        }
        boolean isExists_objectTypeKey = objectInfoHandlerService.isExists_objectTypeKey(param);
        if (!isExists_objectTypeKey){
            log.error("Start add object info, but the object type key doesn't exists");
            return ResponseResult.error(RestErrorCode.ILLEGAL_ARGUMENT, "添加对象类型不存在，请检查！");
        }
        if (param.getPictureDatas() == null || param.getPictureDatas().getImageData() == null) {
            log.error("Start add object info, but picture data is error");
            return ResponseResult.error(RestErrorCode.ILLEGAL_ARGUMENT, "添加对象照片数据为空，请检查！");
        }
        boolean authentication_idCode = objectInfoHandlerService.authentication_idCode(param);
        if (!authentication_idCode){
            log.error("Start add object info, but the idcard format is error");
            return ResponseResult.error(RestErrorCode.ILLEGAL_ARGUMENT, "身份证格式错误，请检查！");
        }
        boolean isExists_idCode = objectInfoHandlerService.isExists_idCode(param);
        if (isExists_idCode){
            log.error("Start add object info, but the idcard already exists");
            return ResponseResult.error(RestErrorCode.ILLEGAL_ARGUMENT, "身份证已存在，请检查！");
        }
        log.info("Start add object info, param is:" + JSONUtil.toJson(param));
        Integer succeed = objectInfoHandlerService.addObjectInfo(param);
        if (succeed == 0) {
            return ResponseResult.init(succeed);
        } else {
            return ResponseResult.error(succeed, "添加对象失败");
        }
    }

    /**
     * 删除对象
     *
     * @param rowkeyList 对象ID列表
     * @return 成功状态【0：删除成功；1：删除失败】
     */
    @ApiOperation(value = "删除对象", response = ResponseResult.class)
    @RequestMapping(value = BigDataPath.OBJECTINFO_DELETE, method = RequestMethod.DELETE)
    @PreAuthorize("hasAuthority('" + BigDataPermission.OBJECT_OPERATION + "')")
    public ResponseResult<Integer> deleteObjectInfo(@RequestBody @ApiParam(value = "删除列表") List<String> rowkeyList) {
        if (rowkeyList == null || rowkeyList.size() == 0) {
            log.error("Start delete object info, but rowkey list is null");
            return ResponseResult.error(RestErrorCode.ILLEGAL_ARGUMENT, "删除列表为空，请检查！");
        }
        log.info("Start delete object info, rowkey list is:" + rowkeyList);
        Integer succeed = objectInfoHandlerService.deleteObjectInfo(rowkeyList);
        if (succeed == 0) {
            return ResponseResult.init(succeed);
        } else {
            return ResponseResult.error(succeed, "删除对象失败");
        }
    }

    /**
     * 修改对象
     *
     * @param param 对象信息
     * @return 成功状态【0：插入成功；1：插入失败】
     */
    @ApiOperation(value = "修改对象", response = ResponseResult.class)
    @RequestMapping(value = BigDataPath.OBJECTINFO_UPDATE, method = RequestMethod.PUT)
    @PreAuthorize("hasAuthority('" + BigDataPermission.OBJECT_OPERATION + "')")
    public ResponseResult<Integer> updateObjectInfo(@RequestBody @ApiParam(value = "修改对象") ObjectInfoParam param) {
        if (param == null) {
            log.error("Start update object info, but param is null");
            return ResponseResult.error(RestErrorCode.ILLEGAL_ARGUMENT, "修改对象为空，请检查！");
        }
        if (StringUtils.isBlank(param.getId())) {
            log.error("Start update object info, but object id is null");
            return ResponseResult.error(RestErrorCode.ILLEGAL_ARGUMENT, "修改对象ID为空，请检查！");
        }
        if (StringUtils.isBlank(param.getObjectTypeKey())) {
            log.error("Start update object info, but object type key is null");
            return ResponseResult.error(RestErrorCode.ILLEGAL_ARGUMENT, "修改对象类型为空，请检查！");
        }
        boolean isExists_objectTypeKey = objectInfoHandlerService.isExists_objectTypeKey(param);
        if (!isExists_objectTypeKey){
            log.error("Start update object info, but the object type key doesn't exists");
            return ResponseResult.error(RestErrorCode.ILLEGAL_ARGUMENT, "修改对象类型不存在，请检查！");
        }
        boolean authentication_idCode = objectInfoHandlerService.authentication_idCode(param);
        if (!authentication_idCode){
            log.error("Start update object info, but the idcard format is error");
            return ResponseResult.error(RestErrorCode.ILLEGAL_ARGUMENT, "身份证格式错误，请检查！");
        }
        String idCade_DB = objectInfoHandlerService.getObjectIdCard(param);
        if (!StringUtils.isBlank(param.getIdcard()) && !param.getIdcard().equals(idCade_DB)){
            boolean isExists_idCode = objectInfoHandlerService.isExists_idCode(param);
            if (isExists_idCode){
                log.error("Start update object info, but the idcard already exists");
                return ResponseResult.error(RestErrorCode.ILLEGAL_ARGUMENT, "身份证已存在，请检查！");
            }
        }
        log.info("Start update object info, param is:" + JSONUtil.toJson(param));
        Integer succeed = objectInfoHandlerService.updateObjectInfo(param);
        if (succeed == 0) {
            return ResponseResult.init(succeed);
        } else {
            return ResponseResult.error(succeed, "修改对象失败");
        }
    }

    /**
     * 更新对象状态值
     *
     * @param objectId 对象ID
     * @param status   状态值
     * @return 返回值为0，表示更新成功，返回值为1，表示更新失败
     */
    @ApiOperation(value = "更新对象状态值", response = ResponseResult.class)
    @ApiImplicitParams(value = {
            @ApiImplicitParam(name = "objectId", value = "对象ID", dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "status", value = "状态码", dataType = "Integer", paramType = "query")
    })
    @RequestMapping(value = BigDataPath.OBJECTINFO_UPDATE_STATUS, method = RequestMethod.GET)
    @PreAuthorize("hasAuthority('" + BigDataPermission.OBJECT_OPERATION + "')")
    public ResponseResult<Integer> updateObjectInfo_status(String objectId, int status) {
        if (StringUtils.isBlank(objectId)) {
            log.error("Start update object status, but object id is null");
            return ResponseResult.error(RestErrorCode.ILLEGAL_ARGUMENT, "修改对象ID为空，请检查！");
        }
        log.info("Start update object status, param is : objectId = " + objectId + ", status = " + status);
        int succeed = objectInfoHandlerService.updateObjectInfo_status(objectId, status);
        if (succeed == 0) {
            return ResponseResult.init(succeed);
        } else {
            return ResponseResult.error(succeed, "更新对象状态值失败");
        }
    }

    /**
     * 根据id查询对象
     *
     * @param objectId 对象ID
     * @return ObjectInfo
     */
    @ApiOperation(value = "根据id查询对象", response = ResponseResult.class)
    @ApiImplicitParam(name = "objectId", value = "对象ID", dataType = "String", paramType = "query")
    @RequestMapping(value = BigDataPath.OBJECTINFO_GET, method = RequestMethod.GET)
    @PreAuthorize("hasAuthority('" + BigDataPermission.OBJECT_VIEW + "')")
    public ResponseResult<ObjectInfo> getObjectInfo(String objectId) {
        if (StringUtils.isBlank(objectId)) {
            log.error("Start get object info, but object id is null");
            return ResponseResult.error(RestErrorCode.ILLEGAL_ARGUMENT, "查询对象ID为空，请检查！");
        }
        log.info("Start get object info, param is : " + objectId);
        ObjectInfo objectInfo = objectInfoHandlerService.getObjectInfo(objectId);
        return ResponseResult.init(objectInfo);
    }

    /**
     * 查询对象
     *
     * @param param 查询条件封装
     * @return ObjectSearchResult
     */
    @ApiOperation(value = "对象查询", response = ObjectSearchResult.class)
    @RequestMapping(value = BigDataPath.OBJECTINFO_SEARCH, method = RequestMethod.POST)
    @PreAuthorize("hasAuthority('" + BigDataPermission.OBJECT_VIEW + "')")
    public ResponseResult<ObjectSearchResult> searchObjectInfo(@RequestBody @ApiParam(value = "查询条件") GetObjectInfoParam param) {
        if (param == null) {
            log.error("Start get object info, but param is null");
            return ResponseResult.error(RestErrorCode.ILLEGAL_ARGUMENT, "查询对象参数为空，请检查！");
        }
        log.info("Start get object info, param is:" + JSONUtil.toJson(param));
        ObjectSearchResult result = objectInfoHandlerService.searchObjectInfo(param);
        return ResponseResult.init(result);
    }

    /**
     * 获取静态库照片
     *
     * @param objectID 对象ID
     * @return byte[]
     */
    @ApiOperation(value = "获取静态库照片", produces = "image/jpeg")
    @ApiImplicitParam(name = "objectID", value = "对象ID", dataType = "String", paramType = "query")
    @RequestMapping(value = BigDataPath.OBJECTINFO_GET_PHOTOBYKEY, method = RequestMethod.GET)
    public ResponseEntity<byte[]> getObjectPhoto(String objectID) {
        if (StringUtils.isBlank(objectID)) {
            log.error("Start get object photo, but object id null");
            return ResponseEntity.badRequest().contentType(MediaType.IMAGE_JPEG).body(null);
        }
        log.info("Start get object photo, param is : " + objectID);
        byte[] photo = objectInfoHandlerService.getPhotoByKey(objectID);
        return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(photo);
    }

    /**
     * 获取特征值
     *
     * @param id 对象ID
     * @return PictureData
     */
    @ApiOperation(value = "获取特征值<PictureData>", response = PictureData.class)
    @ApiImplicitParam(name = "id", value = "对象ID", dataType = "String", paramType = "query")
    @RequestMapping(value = BigDataPath.OBJECTINFO_GET_FEATURE, method = RequestMethod.GET)
    public ResponseResult<PictureData> getFeature(String id) {
        if (!IsEmpty.strIsRight(id)) {
            log.error("Start get object picture data, but object id is null");
            return ResponseResult.error(RestErrorCode.ILLEGAL_ARGUMENT, "查询对象ID为空，请检查！");
        }
        log.info("Start get object picture data, param is : " + id);
        PictureData result = objectInfoHandlerService.getFeature(id);
        return ResponseResult.init(result);
    }

    /**
     * 获取更多对象
     *
     * @param param 查询条件封装
     * @return ObjectSearchResult
     */
//    @ApiOperation(value = "获取更多对象", response = ObjectSearchResult.class)
    @RequestMapping(value = BigDataPath.STAREPO_GET_SEARCHRESULT, method = RequestMethod.POST)
    @PreAuthorize("hasAuthority('" + BigDataPermission.OBJECT_VIEW + "')")
    public ResponseResult<ObjectSearchResult> getRocordOfObjectInfo(@RequestBody @ApiParam(value = "查询记录") SearchRecordParam param) {
        if (param == null) {
            return null;
        }
        ObjectSearchResult result = objectInfoHandlerService.getRocordOfObjectInfo(param);
        return ResponseResult.init(result);
    }

    /**
     * 导出重点人员
     *
     * @param param 查询条件封装
     * @return 导出Word文本
     */
    @ApiOperation(value = "生成重点人员Word", response = String.class)
    @RequestMapping(value = BigDataPath.STAREPO_CREATE_WORD, method = RequestMethod.POST)
    @PreAuthorize("hasAuthority('" + BigDataPermission.OBJECT_OPERATION + "')")
    public ResponseResult<String> createPeoplesWord(@RequestBody @ApiParam(value = "历史查询参数") SearchRecordParam param) {
        if (param == null) {
            log.error("Start create emphasis personnel word, but param error");
            return ResponseResult.error(RestErrorCode.ILLEGAL_ARGUMENT, "Param is null");
        }
        if (param.getStart() == 0) {
            log.error("Start create emphasis personnel word, but start = 0");
            return ResponseResult.error(RestErrorCode.ILLEGAL_ARGUMENT, "Param: start = 0");
        }
        if (param.getSize() == 0) {
            log.error("Start create emphasis personnel word, but size = 0");
            return ResponseResult.error(RestErrorCode.ILLEGAL_ARGUMENT, "Param: size = 0");
        }
        if (StringUtils.isBlank(param.getTotalSearchId())) {
            log.error("Start create emphasis personnel word, but total search id is null");
            return ResponseResult.error(RestErrorCode.ILLEGAL_ARGUMENT, "Param: total search id is null");
        }
        if (param.getSubQueryParamList() == null) {
            log.error("Start create emphasis personnel word, but search result list is null");
            return ResponseResult.error(RestErrorCode.ILLEGAL_ARGUMENT, "Param: search result list is null");
        }
        log.info("Start create emphasis personnel word, param is : " + JSONUtil.toJson(param));
        String rowkey_file = objectInfoHandlerService.exportPeoples(param);
        return ResponseResult.init(rowkey_file);
    }

    /**
     * 导出重点人员Word
     *
     * @param fileAddress 文件地址
     * @return 文件二进制
     */
    @ApiOperation(value = "导出重点人员Word", response = byte[].class)
    @ApiImplicitParam(name = "fileAddress", value = "文件地址", dataType = "String", paramType = "query")
    @RequestMapping(value = BigDataPath.STAREPO_EXPORT_WORD, method = RequestMethod.GET)
    @PreAuthorize("hasAuthority('" + BigDataPermission.OBJECT_OPERATION + "')")
    public ResponseEntity<byte[]> exportPeoplesWord(@ApiParam(value = "文件地址") String fileAddress) {
        if (fileAddress == null) {
            log.error("Start export emphasis personnel word, but file address error");
            return null;
        }
        log.info("Start export emphasis personnel word, param is : " + fileAddress);
        byte[] file = objectInfoHandlerService.getDataFromHBase(fileAddress, SearchResultTable.STAREPO_COLUMN_FILE);
        HttpHeaders headers = new HttpHeaders();
        try {
            headers.add("Content-Disposition",
                    "attachment;filename=" + new String(fileAddress.getBytes("UTF-8"), "ISO8859-1"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_OCTET_STREAM).headers(headers).body(file);
    }

    /**
     * 获取搜索原图
     *
     * @param picID 图片ID
     * @return 图片数据
     */
//    @ApiOperation(value = " 获取搜索原图", produces = "image/jpeg")
    @ApiImplicitParam(name = "picID", value = "图片ID", dataType = "String", paramType = "query")
    @RequestMapping(value = BigDataPath.STAREPO_GET_SEARCHPHOTO, method = RequestMethod.GET)
    @PreAuthorize("hasAuthority('" + BigDataPermission.OBJECT_VIEW + "')")
    public ResponseEntity<byte[]> getSearchPhots(@ApiParam(value = "图片ID") String picID) {
        if (!IsEmpty.strIsRight(picID)) {
            return null;
        }
        byte[] photo = objectInfoHandlerService.getDataFromHBase(picID, SearchResultTable.STAREPO_COLUMN_PICTURE);
        return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(photo);
    }

    /**
     * 统计常住人口
     *
     * @return int 常住人口数量
     */
    @ApiOperation(value = "统计常住人口", response = ResponseResult.class)
    @RequestMapping(value = BigDataPath.OBJECTINFO_COUNT_STATUS, method = RequestMethod.GET)
    public ResponseResult<Integer> permanentPopulationCount() {
        log.info("Start count permanent population");
        int count = objectInfoHandlerService.permanentPopulationCount();
        return ResponseResult.init(count);
    }

    /**
     * 迁出人口数量统计
     *
     * @param start_time 起始统计时间
     * @param end_time   结束统计时间
     * @return List<EmigrationCount> 统计列表
     */
    @RequestMapping(value = BigDataPath.STAREPO_COUNT_EMIGRATION, method = RequestMethod.GET)
    public List<PeopleManagerCount> emigrationCount(String start_time, String end_time) {
        log.info("Start count emigration population, param is : start_time = " + start_time + ", end_time = " + end_time);
        return objectInfoHandlerService.emigrationCount(start_time, end_time);
    }
}
