package com.hzgc.service.facedispatch.starepo.service;

import com.hzgc.common.util.basic.UuidUtil;
import com.hzgc.jniface.FaceJNI;
import com.hzgc.service.facedispatch.starepo.bean.*;
import com.hzgc.service.facedispatch.starepo.dao.ObjectInfoMapper;
import com.hzgc.service.facedispatch.starepo.dao.ObjectTypeMapper;
import com.hzgc.service.facedispatch.starepo.model.FilterField;
import com.hzgc.service.facedispatch.starepo.model.ObjectFeature;
import com.hzgc.service.facedispatch.starepo.model.ObjectInfo;
import com.hzgc.service.facedispatch.starepo.model.ObjectType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class FaceSearchService {
    @Autowired
    private ObjectInfoMapper objectInfoMapper;

    @Autowired
    private ObjectTypeMapper objectTypeMapper;

    @Autowired
    private FaceFeatureMemoryCache memoryCache;

    /**
     * 查询对象（以图搜图）
     *
     * @param param 查询条件封装
     * @return ObjectSearchResult
     */
    public ObjectSearchResult search(GetObjectInfoParam param) {
        ObjectSearchResult objectSearchResult = null;
        if (param.getPictureDataList() != null && param.getPictureDataList().size() > 0) {
            if (!param.isSinglePerson()) {
                //多图搜索
                this.multiple_search(param);
            } else {
                //单图搜索
                List<PersonObject> personObjectList = this.single_search(param);
                objectSearchResult = this.assembly(personObjectList);
            }
        } else {
            //无图搜索
            List<PersonObject> personObjectList = this.noPic_search(param);
            objectSearchResult = this.assembly(personObjectList);

        }
        return objectSearchResult;
    }

    /**
     * 多图搜索
     *
     * @param param 查询条件封装
     */
    private void multiple_search(GetObjectInfoParam param) {
    }

    /**
     * 单图搜索
     *
     * @param param 查询条件封装
     * @return List<PersonObject>
     */
    private List<PersonObject> single_search(GetObjectInfoParam param) {
        if (param.getPictureDataList() == null || param.getPictureDataList().size() == 0) {
            return null;
        }
        if (param.getPictureDataList().get(0) == null || param.getPictureDataList().get(0).getFeature() == null) {
            return null;
        }
        if (param.getPictureDataList().get(0).getFeature().getFeature() == null || param.getPictureDataList().get(0).getFeature().getFeature().length == 0) {
            return null;
        }
        float[] feature = param.getPictureDataList().get(0).getFeature().getFeature();
        
        List<ConcurrentHashMap<String, ObjectFeature>> memoryCacheMap;
        if (param.getObjectTypeKeyList() != null && param.getObjectTypeKeyList().size() > 0) {
            memoryCacheMap = memoryCache.getMemoryByTypeIds(param.getObjectTypeKeyList());
        } else {
            memoryCacheMap = memoryCache.getAllMemory();
        }
        if (memoryCacheMap == null) {
            return null;
        }
        List<ObjectFeature> memoryCacheList = new ArrayList<>();
        for (ConcurrentHashMap<String, ObjectFeature> map : memoryCacheMap) {
            if (map != null) {
                memoryCacheList.addAll(map.values());
            }
        }
        List<ObjectFeature> objectFeatureList = new ArrayList<>();
        for (ObjectFeature objectFeature : memoryCacheList) {
            float similarity = FaceJNI.featureCompare(objectFeature.getFeature(), feature);
            if (similarity >= param.getSimilarity()) {
                objectFeature.setSimilarity(similarity);
                objectFeatureList.add(objectFeature);
            }
        }
        Collections.sort(objectFeatureList);
        List<ObjectFeature> finalList = new ArrayList<>();
        List<String> idList = new ArrayList<>();
        for (int i = param.getStart(); i < param.getStart() + param.getLimit(); i++) {
            finalList.add(objectFeatureList.get(i));
            idList.add(objectFeatureList.get(i).getId());
        }
        Map<String, Float> simMap = new HashMap<>();
        for (ObjectFeature objectFeature : finalList) {
            simMap.put(objectFeature.getId(), objectFeature.getSimilarity());
        }

        FilterField filterField = new FilterField();
        filterField.setIdList(idList);
        filterField.setName(param.getObjectName());
        filterField.setIdCard(param.getIdcard());
        filterField.setSex(param.getSex());
        filterField.setCreator(param.getCreator());
        filterField.setCreatePhone(param.getCreatorConractWay());
        List<ObjectInfo> objectInfoList = objectInfoMapper.searchObject(filterField);
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        List<PersonObject> personObjectList = new ArrayList<>();
        List<String> typeIdList = new ArrayList<>();
        for (ObjectInfo info : objectInfoList) {
            PersonObject person = new PersonObject();
            person.setObjectID(info.getId());
            person.setName(info.getName());
            person.setObjectTypeKey(info.getTypeid());
            if (!typeIdList.contains(info.getTypeid())) {
                typeIdList.add(info.getTypeid());
            }
            person.setSex(info.getSex());
            person.setIdcard(info.getIdcard());
            person.setCreator(info.getCreator());
            person.setCreatorConractWay(info.getCreatephone());
            String createTime = sdf.format(info.getCreatetime());
            person.setCreateTime(createTime);
            person.setReason(info.getReason());
            person.setSimilarity(simMap.get(info.getId()));
            personObjectList.add(person);
        }
        if (personObjectList.size() > 0) {
            Map<String, String> typeNameMapping = new HashMap<>();
            for (String typeId : typeIdList) {
                ObjectType type = objectTypeMapper.selectObjectTypeById(typeId);
                typeNameMapping.put(typeId, type.getName());
            }
            for (PersonObject object : personObjectList) {
                object.setObjectTypeName(typeNameMapping.get(object.getObjectTypeKey()));
            }
        }
        return personObjectList;
    }

    /**
     * 无图搜索
     *
     * @param param 查询条件封装
     * @return List<PersonObject>
     */
    private List<PersonObject> noPic_search(GetObjectInfoParam param) {
        FilterField filterField = new FilterField();
        filterField.setName(param.getObjectName());
        filterField.setTypeIdList(param.getObjectTypeKeyList());
        filterField.setIdCard(param.getIdcard());
        filterField.setSex(param.getSex());
        filterField.setCreator(param.getCreator());
        filterField.setCreatePhone(param.getCreatorConractWay());
        List<ObjectInfo> list = objectInfoMapper.searchObject(filterField);
        List<ObjectInfo> objectInfoList = new ArrayList<>();
        for (int i = param.getStart(); i < param.getStart() + param.getLimit(); i++) {
            objectInfoList.add(list.get(i));
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        List<PersonObject> personObjectList = new ArrayList<>();
        List<String> typeIdList = new ArrayList<>();
        for (ObjectInfo info : objectInfoList) {
            PersonObject person = new PersonObject();
            person.setObjectID(info.getId());
            person.setName(info.getName());
            person.setObjectTypeKey(info.getTypeid());
            if (!typeIdList.contains(info.getTypeid())) {
                typeIdList.add(info.getTypeid());
            }
            person.setSex(info.getSex());
            person.setIdcard(info.getIdcard());
            person.setCreator(info.getCreator());
            person.setCreatorConractWay(info.getCreatephone());
            String createTime = sdf.format(info.getCreatetime());
            person.setCreateTime(createTime);
            person.setReason(info.getReason());
            personObjectList.add(person);
        }
        if (personObjectList.size() > 0) {
            Map<String, String> typeNameMapping = new HashMap<>();
            for (String typeId : typeIdList) {
                ObjectType type = objectTypeMapper.selectObjectTypeById(typeId);
                typeNameMapping.put(typeId, type.getName());
            }
            for (PersonObject object : personObjectList) {
                object.setObjectTypeName(typeNameMapping.get(object.getObjectTypeKey()));
            }
        }
        return personObjectList;
    }

    private ObjectSearchResult assembly(List<PersonObject> list) {
        String uuid = UuidUtil.getUuid();
        PersonSingleResult personSingleResult = new PersonSingleResult();
        personSingleResult.setSearchId(uuid);
        personSingleResult.setTotal(list.size());
        personSingleResult.setObjectInfoBeans(list);
        personSingleResult.setSingleObjKeyResults(this.shift(list));
        List<PersonSingleResult> personSingleResultList = new ArrayList<>();
        personSingleResultList.add(personSingleResult);
        ObjectSearchResult result = new ObjectSearchResult();
        result.setSearchId(uuid);
        result.setSingleSearchResults(personSingleResultList);
        return result;
    }
    
    /**
     * List<PersonObject> 转换为 List<PersonObjectGroupByPkey>
     *
     * @param list List<PersonObject>
     * @return List<PersonObjectGroupByPkey>
     */
    private List<PersonObjectGroupByPkey> shift(List<PersonObject> list) {
        List<PersonObjectGroupByPkey> personObjectGroupByPkeyList = new ArrayList<>();
        List<String> typeIdList = new ArrayList<>();
        for (PersonObject object : list) {
            if (!typeIdList.contains(object.getObjectTypeKey())) {
                typeIdList.add(object.getObjectTypeKey());
            }
        }
        Map<String, String> typeNameMapping = new HashMap<>();
        for (String typeId : typeIdList) {
            ObjectType type = objectTypeMapper.selectObjectTypeById(typeId);
            typeNameMapping.put(typeId, type.getName());
        }
        for (String typeId : typeIdList) {
            PersonObjectGroupByPkey groupByPkey = new PersonObjectGroupByPkey();
            groupByPkey.setObjectTypeKey(typeId);
            String typeName = typeNameMapping.get(typeId);
            groupByPkey.setObjectTypeName(typeName);
            List<PersonObject> personObjectList = new ArrayList<>();
            for (PersonObject object : list) {
                if (typeId.equals(object.getObjectTypeKey())) {
                    personObjectList.add(object);
                }
            }
            groupByPkey.setTotal(personObjectList.size());
            groupByPkey.setPersonObjectList(personObjectList);
            personObjectGroupByPkeyList.add(groupByPkey);
        }
        return personObjectGroupByPkeyList;
    }

}
