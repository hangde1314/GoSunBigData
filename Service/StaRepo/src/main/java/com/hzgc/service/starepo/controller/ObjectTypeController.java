package com.hzgc.service.starepo.controller;

import com.hzgc.common.util.empty.IsEmpty;
import com.hzgc.common.util.json.JSONUtil;
import com.hzgc.service.starepo.bean.param.ObjectTypeParam;
import com.hzgc.service.starepo.service.ObjectTypeService;
import com.hzgc.service.util.error.RestErrorCode;
import com.hzgc.service.util.response.ResponseResult;
import com.hzgc.service.util.rest.BigDataPath;
import com.hzgc.service.util.rest.BigDataPermission;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@Api(value = "/objectType", tags = "对象类型")
public class ObjectTypeController {

    @Autowired
    private ObjectTypeService objectTypeService;

    /**
     * 添加objectType
     *
     * @param objectTypeParam add objectType对象
     * @return boolean
     */
    @ApiOperation(value = "添加对象类型", response = ResponseResult.class)
    @RequestMapping(value = BigDataPath.TYPE_ADD, method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    @PreAuthorize("hasAuthority('" + BigDataPermission.OBJECT_OPERATION + "')")
    public ResponseResult<Boolean> addObjectType(@RequestBody @ApiParam(value = "对象类型") ObjectTypeParam objectTypeParam) {
        if (objectTypeParam == null) {
            log.error("Start add object type, but param is null");
            return ResponseResult.error(RestErrorCode.ILLEGAL_ARGUMENT, "添加对象类型信息为空，请检查！");
        }
        String name = objectTypeParam.getObjectTypeName();
        if (StringUtils.isBlank(name)) {
            log.error("Start add object type, but object type name is null");
            return ResponseResult.error(RestErrorCode.ILLEGAL_ARGUMENT,"添加对象类型名称为空，请检查！");
        }
        boolean isExists_objectTypeName = objectTypeService.isExists_objectTypeName(name);
        if (isExists_objectTypeName){
            log.error("Start add object type, but the object type name already exists");
            return ResponseResult.error(RestErrorCode.ILLEGAL_ARGUMENT,"添加对象类型名称已存在，请检查！");
        }
        log.info("Start add object type, param is:" + JSONUtil.toJson(objectTypeParam));
        boolean success = objectTypeService.addObjectType(objectTypeParam);
        return ResponseResult.init(success);
    }

    /**
     * 删除objectType
     *
     * @param objectTypeKeyList 类型Key列表
     * @return boolean
     */
    @ApiOperation(value = "删除对象类型", response = ResponseResult.class)
    @RequestMapping(value = BigDataPath.TYPE_DELETE, method = RequestMethod.DELETE)
    @PreAuthorize("hasAuthority('" + BigDataPermission.OBJECT_OPERATION + "')")
    public ResponseResult<Boolean> deleteObjectType(@RequestBody @ApiParam(value = "对象类型key列表") List<String> objectTypeKeyList) {
        if (!IsEmpty.listIsRight(objectTypeKeyList)) {
            log.error("Start delete object type list, but param is null");
            return ResponseResult.error(RestErrorCode.ILLEGAL_ARGUMENT, "删除列表为空，请检查!");
        }
        log.info("Start delete object type list, param is : " + objectTypeKeyList);
        boolean success = objectTypeService.deleteObjectType(objectTypeKeyList);
        return ResponseResult.init(success);
    }

    /**
     * 修改ObjectType
     *
     * @param objectTypeParam update objectType对象
     * @return boolean
     */
    @ApiOperation(value = "修改对象类型", response = ResponseResult.class)
    @RequestMapping(value = BigDataPath.TYPE_UPDATE, method = RequestMethod.PUT, consumes = "application/json", produces = "application/json")
    @PreAuthorize("hasAuthority('" + BigDataPermission.OBJECT_OPERATION + "')")
    public ResponseResult<Boolean> updateObjectType(@RequestBody @ApiParam(value = "对象类型") ObjectTypeParam objectTypeParam) {
        if (objectTypeParam == null) {
            log.error("Start update object type, but param is null");
            return ResponseResult.error(RestErrorCode.ILLEGAL_ARGUMENT, "修改对象类型信息为空，请检查！");
        }
        String objectTypeKey = objectTypeParam.getObjectTypeKey();
        if (StringUtils.isBlank(objectTypeKey)) {
            log.error("Start update object type, but object type key is null");
            return ResponseResult.error(RestErrorCode.ILLEGAL_ARGUMENT, "修改对象类型ID为空，请检查！");
        }
        String name = objectTypeParam.getObjectTypeName();
        if (StringUtils.isBlank(name)) {
            log.error("Start update object type, but object type name is null");
            return ResponseResult.error(RestErrorCode.ILLEGAL_ARGUMENT, "修改对象类型名称为空，请检查！");
        }
        String name_DB = objectTypeService.getObjectTypeName(objectTypeKey);
        if (!name.equals(name_DB)){
            boolean isExists_objectTypeName = objectTypeService.isExists_objectTypeName(name);
            if (isExists_objectTypeName){
                log.error("Start update object type, but the object type name already exists");
                return ResponseResult.error(RestErrorCode.ILLEGAL_ARGUMENT,"修改对象类型名称已存在，请检查！");
            }
        }
        log.info("Start update object type, param is : " + JSONUtil.toJson(objectTypeParam));
        boolean success = objectTypeService.updateObjectType(objectTypeParam);
        return ResponseResult.init(success);
    }

    /**
     * 查询objectType
     *
     * @param start 起始页码
     * @param limit 每页行数
     * @return List<ObjectTypeParam>
     */
    @ApiOperation(value = "查询对象类型", response = ResponseResult.class)
    @ApiImplicitParams(value = {
            @ApiImplicitParam(name = "start", value = "起始行数", dataType = "Integer", paramType = "query"),
            @ApiImplicitParam(name = "limit", value = "分页行数", dataType = "Integer", paramType = "query")
    })
    @RequestMapping(value = BigDataPath.TYPE_SEARCH, method = RequestMethod.GET)
    @PreAuthorize("hasAuthority('" + BigDataPermission.OBJECT_VIEW + "')")
    public ResponseResult<List<ObjectTypeParam>> searchObjectType(Integer start, Integer limit) {
        log.info("Start search object type, receive param is : start = " + start + "; limit = " + limit);
        if (start == null) {
            start = 0;
        }
        if (limit == null || limit == 0) {
            limit = 20;
        } else {
            limit = start + limit;
        }
        log.info("Start search object type, param is : start = " + start + "; end = " + limit);
        List<ObjectTypeParam> objectTypeParamList = objectTypeService.searchObjectType(start, limit);
        int count = objectTypeService.countObjectType();
        return ResponseResult.init(objectTypeParamList, count);
    }

    /**
     * 查询objectTypeName
     *
     * @param objectTypeKeys 对象类型key数组
     * @return Map
     */
    @ApiOperation(value = "查询对象类型名称", response = ResponseResult.class)
    @ApiImplicitParam(name = "objectTypeKeys", value = "对象类型key数组", dataType = "List", paramType = "query")
    @RequestMapping(value = BigDataPath.TYPE_SEARCH_NAMES, method = RequestMethod.POST)
    @PreAuthorize("hasAuthority('" + BigDataPermission.OBJECT_VIEW + "')")
    public ResponseResult<Map> searchObjectTypeNames(@RequestBody List<String> objectTypeKeys) {
        if (objectTypeKeys == null || objectTypeKeys.size() <= 0) {
            log.error("Start search object type names, but param is null");
            return ResponseResult.error(RestErrorCode.ILLEGAL_ARGUMENT);
        }
        log.info("Start search object type names, param is : " + objectTypeKeys);
        Map map = objectTypeService.searchObjectTypeNames(objectTypeKeys);
        return ResponseResult.init(map);
    }
}
