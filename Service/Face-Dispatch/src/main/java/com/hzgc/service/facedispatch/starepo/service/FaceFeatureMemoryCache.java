package com.hzgc.service.facedispatch.starepo.service;

import com.hzgc.jniface.FaceFunction;
import com.hzgc.service.facedispatch.starepo.dao.ObjectInfoMapper;
import com.hzgc.service.facedispatch.starepo.model.ObjectFeature;
import com.hzgc.service.facedispatch.starepo.model.ObjectInfo;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class FaceFeatureMemoryCache {
    @Autowired
    private ObjectInfoMapper objectInfoMapper;

    // <key:typeId, value:<key:id, value:ObjectFeature>>
    private volatile ConcurrentHashMap<String, ConcurrentHashMap<String, ObjectFeature>> memoryCache = new ConcurrentHashMap<>();

    private final boolean FAILED = false;
    private final boolean SUCCESS = true;

    public ConcurrentHashMap<String, ConcurrentHashMap<String, ObjectFeature>> getMemoryCache() {
        return memoryCache;
    }

    /**
     * 将数据库中所有对象中的id、typeId、feature字段写入内存
     */
    public void writeMemory() {
        List<ObjectInfo> list = objectInfoMapper.selectAllObject();
        for (ObjectInfo info : list) {
            ObjectFeature feature = new ObjectFeature();
            feature.setId(info.getId());
            feature.setTypeId(info.getTypeid());
            feature.setFeature(FaceFunction.base64Str2floatFeature(info.getFeature()));
            feature.setBitfea(FaceFunction.base64Str2floatFeature(info.getBitfea()));
            ConcurrentHashMap<String, ObjectFeature> typeMap = memoryCache.get(info.getTypeid());
            if (typeMap == null) {
                typeMap = new ConcurrentHashMap<>();
            }
            typeMap.put(info.getId(), feature);
            memoryCache.put(info.getTypeid(), typeMap);
        }
    }

    /**
     * 单个特征对象写入内存
     */
    public boolean writeMemory(ObjectFeature value) {
        if (value == null) {
            return FAILED;
        }
        if (StringUtils.isBlank(value.getId())) {
            return FAILED;
        }
        if (StringUtils.isBlank(value.getTypeId())) {
            return FAILED;
        }
        if (memoryCache == null) {
            ConcurrentHashMap<String, ObjectFeature> typeMap = new ConcurrentHashMap<>();
            typeMap.put(value.getId(), value);
            memoryCache.put(value.getTypeId(), typeMap);
        } else {
            ConcurrentHashMap<String, ObjectFeature> typeMap = memoryCache.get(value.getTypeId());
            if (typeMap == null) {
                typeMap = new ConcurrentHashMap<>();
            }
            typeMap.put(value.getId(), value);
            memoryCache.put(value.getTypeId(), typeMap);
        }
        return SUCCESS;
    }

    /**
     * 特征对象批量写入内存
     */
    public boolean writeMemory(List<ObjectFeature> list) {
        if (list == null || list.size() == 0) {
            return FAILED;
        }
        for (ObjectFeature value : list) {
            boolean boo = this.writeMemory(value);
            if (!boo) {
                return FAILED;
            }
        }
        return SUCCESS;
    }

    /**
     * 从内存中删除单个特征对象
     */
    public boolean deleteMemory(String typeId, String id) {
        if (StringUtils.isBlank(typeId)) {
            return FAILED;
        }
        if (StringUtils.isBlank(id)) {
            return FAILED;
        }
        ConcurrentHashMap<String, ObjectFeature> typeMap = memoryCache.get(typeId);
        if (typeMap != null) {
            typeMap.remove(id);
        }
        return SUCCESS;
    }

    /**
     * 从内存中批量删除特征对象
     */
    public boolean deleteMemory(List<ObjectFeature> list) {
        if (list == null || list.size() == 0) {
            return FAILED;
        }
        for (ObjectFeature value : list) {
            if (StringUtils.isBlank(value.getTypeId())) {
                return FAILED;
            }
            if (StringUtils.isBlank(value.getId())) {
                return FAILED;
            }
            ConcurrentHashMap<String, ObjectFeature> typeMap = memoryCache.get(value.getTypeId());
            if (typeMap != null) {
                typeMap.remove(value.getId());
            }
        }
        return SUCCESS;
    }

    /**
     * 获取内存中单个typeId下的所有特征对象
     */
    public ConcurrentHashMap<String, ObjectFeature> getMemoryByTypeId(String typeId) {
        if (StringUtils.isBlank(typeId)) {
            return new ConcurrentHashMap<>();
        }
        return memoryCache.get(typeId);
    }

    /**
     * 获取内存中多个typeId下的所有特征对象
     */
    public List<ConcurrentHashMap<String, ObjectFeature>> getMemoryByTypeIds(List<String> typeIdList) {
        if (typeIdList == null || typeIdList.size() == 0) {
            return new ArrayList<>();
        }
        List<ConcurrentHashMap<String, ObjectFeature>> list = new ArrayList<>();
        for (String typeId : typeIdList) {
            ConcurrentHashMap<String, ObjectFeature> typeMap = this.getMemoryByTypeId(typeId);
            list.add(typeMap);
        }
        return list;
    }

    /**
     * 获取内存中单个特征对象
     */
    public ObjectFeature getObjectFeature(String typeId, String id) {
        if (StringUtils.isBlank(typeId)) {
            return new ObjectFeature();
        }
        if (StringUtils.isBlank(id)) {
            return new ObjectFeature();
        }
        if (memoryCache == null) {
            return new ObjectFeature();
        }
        ConcurrentHashMap<String, ObjectFeature> typeMap = memoryCache.get(typeId);
        if (typeMap == null) {
            return new ObjectFeature();
        }
        return typeMap.get(id);
    }

    /**
     * 获取内存中全部特征对象
     */
    public List<ConcurrentHashMap<String, ObjectFeature>> getAllMemory() {
        List<ConcurrentHashMap<String, ObjectFeature>> list = new ArrayList<>();
        list.addAll(memoryCache.values());
        return list;
    }
}
