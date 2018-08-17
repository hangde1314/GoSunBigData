package com.hzgc.service.starepo.service;

import com.hzgc.common.util.json.JSONUtil;
import com.hzgc.service.starepo.bean.param.ObjectTypeParam;
import com.hzgc.service.starepo.dao.PhoenixDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Slf4j
public class ObjectTypeService {

    @Autowired
    private PhoenixDao phoenixDao;

    /**
     * 添加objectType
     *
     * @param param add objectType对象
     * @return boolean
     */
    public boolean addObjectType(ObjectTypeParam param) {
        String name = param.getObjectTypeName();
        String creator = param.getCreator();
        String remark = param.getRemark();
        return phoenixDao.addObjectType(name, creator, remark);
    }

    /**
     * 对象类型名称唯一性判断
     * true:存在 false:不存在
     */
    public boolean isExists_objectTypeName(String name) {
        List<String> names = phoenixDao.getAllObjectTypeNames();
        log.info("Start add/update object type, get all the object type names in the database first: "
                + JSONUtil.toJson(names));
        if (names.contains(name)){
            return true;
        }
        return false;
    }

    /**
     * 删除objectType
     *
     * @param objectTypeKeyList 类型Key列表
     * @return boolean
     */
    public boolean deleteObjectType(List<String> objectTypeKeyList) {
        return phoenixDao.deleteObjectType(objectTypeKeyList);
    }

    /**
     * 修改ObjectType
     *
     * @param param update objectType对象
     * @return boolean
     */
    public boolean updateObjectType(ObjectTypeParam param) {
        String id = param.getObjectTypeKey();
        String name = param.getObjectTypeName();
        String creator = param.getCreator();
        String remark = param.getRemark();
        return phoenixDao.updateObjectType(id, name, creator, remark);
    }

    /**
     * 查询objectType
     *
     * @param start 查询起始位置
     * @param end  查询结束位置
     * @return List<ObjectTypeParam>
     */
    public List<ObjectTypeParam> searchObjectType(int start, int end) {
        return phoenixDao.searchObjectType(start, end);
    }

    /**
     * 统计对象类型数量
     *
     * @return 对象类型数量
     */
    public int countObjectType() {
        return phoenixDao.countObjectType();
    }

    /**
     * 查询objectTypeName
     *
     * @param objectTypeKeys 对象类型key数组
     * @return Map
     */
    public Map<String, String> searchObjectTypeNames(List<String> objectTypeKeys){
        return phoenixDao.searchTypeNames(objectTypeKeys);
    }

    public String getObjectTypeName(String objectTypeKey) {
        List<String> list = new ArrayList<>();
        list.add(objectTypeKey);
        Map<String, String> map = searchObjectTypeNames(list);
        return map.get(objectTypeKey);
    }
}
