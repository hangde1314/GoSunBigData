package com.hzgc.service.facedispatch.starepo.service;

import com.hzgc.common.util.json.JacksonUtil;
import com.hzgc.jniface.FaceJNI;
import com.hzgc.jniface.PictureData;
import com.hzgc.service.facedispatch.starepo.dao.ObjectInfoMapper;
import com.hzgc.service.facedispatch.starepo.dao.ObjectTypeMapper;
import com.hzgc.service.facedispatch.starepo.model.ObjectFeature;
import com.hzgc.service.facedispatch.starepo.model.ObjectInfo;
import com.hzgc.service.facedispatch.starepo.model.ObjectInfoVO;
import com.hzgc.service.facedispatch.starepo.model.ObjectType;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ObjectInfoService {
    @Autowired
    private ObjectInfoMapper objectInfoMapper;

    @Autowired
    private ObjectTypeMapper objectTypeMapper;

    @Autowired
    private StarepoKafkaProducer starepoKafkaProducer;

    @Autowired
    private FaceFeatureMemoryCache memoryCache;

    @Autowired
    private RestTemplate restTemplate;

    private final int FAILED = 0;

    private final int SUCCESS = 1;

    /**
     * 判断此ID是否存在
     *
     * @param typeId 对象类型ID
     * @return ture:存在  false:不存在
     */
    public boolean isExists_objectTypeId(String typeId) {
        ObjectType objectType = objectTypeMapper.selectObjectTypeById(typeId);
        return objectType != null && StringUtils.isNotBlank(objectType.getName());
    }

    /**
     * 获取此ID类型名称
     *
     * @param typeId 对象类型ID
     * @return ture:存在  false:不存在
     */
    public String getObjectTypeId(String typeId) {
        ObjectType objectType = objectTypeMapper.selectObjectTypeById(typeId);
        return objectType.getName();
    }

    /**
     * 认证身份证格式是否正确
     *
     * @param idCard 身份证ID
     * @return true:正确  false:不正确
     */
    public boolean authentication_idCard(String idCard) {
        if (idCard == null || idCard.isEmpty() || idCard.length() != 18) {
            return false;
        }
        String regEX = "^[1-9]\\d{5}[1-9]\\d{3}((0\\d)|(1[0-2]))(([0|1|2]\\d)|3[0-1])\\d{3}([0-9]|X)$";
        Pattern pattern = Pattern.compile(regEX);
        Matcher matcher = pattern.matcher(idCard);
        return matcher.matches();
    }

    /**
     * 身份证唯一性判断
     *
     * @param idCard 身份证
     * @return true:存在  false:不存在
     */
    public boolean isExists_idCard(String idCard) {
        List<String> idCards = objectInfoMapper.selectAllObjectIdCard();
        for (String str : idCards) {
            if (str.equals(idCard)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 添加对象信息
     *
     * @param objectInfo 对象信息
     * @return
     */
    public Integer addObjectInfo(ObjectInfo objectInfo) {
        Integer i = objectInfoMapper.addObjectSelective(objectInfo);
        //向告警中同步数据
        if (i == SUCCESS) {
            float[] feature = FaceJNI.string2floatArray(objectInfo.getFeature());
            ObjectFeature objectFeature = new ObjectFeature();
            objectFeature.setId(objectInfo.getId());
            objectFeature.setTypeId(objectInfo.getTypeid());
            objectFeature.setFeature(feature);
            StarepoSendKafkaObject object = new StarepoSendKafkaObject();
            object.setFeature(feature);
            object.setPkey(objectInfo.getTypeid());
            object.setRowkey(objectInfo.getId());
            if (feature != null && feature.length > 0) {
                boolean boo = memoryCache.writeMemory(objectFeature);
                if (boo) {
                    starepoKafkaProducer.sendKafkaMessage(
                            StarepoKafkaProducer.TOPIC,
                            StarepoKafkaProducer.ADD,
                            JacksonUtil.toJson(object));
                } else {
                    return FAILED;
                }
            }
        }
        return i;
    }

    /**
     * 删除对象的信息
     *
     * @param idList 对象ID列表
     * @return
     */
    public Integer deleteObjectInfo(List<String> idList) {
        Map<String, String> map = new HashMap<>();
        for (String id : idList){
            ObjectInfo info = objectInfoMapper.selectObjectById(id);
            if (info == null){
                return FAILED;
            }
            map.put(id, info.getTypeid());
        }
        int status = objectInfoMapper.deleteObjectByIdBatch(idList);
        //向告警中同步数据
        if (status == idList.size()) {
            for (String id : idList) {
                boolean boo = memoryCache.deleteMemory(map.get(id), id);
                if (boo) {
                    starepoKafkaProducer.sendKafkaMessage(StarepoKafkaProducer.TOPIC, StarepoKafkaProducer.DELETE, id);
                } else {
                    return FAILED;
                }
            }
        }
        return status;
    }

    /**
     * 修改对象的信息
     *
     * @param objectInfo 对象信息
     * @return
     */
    public Integer updateObjectInfo(ObjectInfo objectInfo) {
        Integer i = objectInfoMapper.updateObjectSelective(objectInfo);
        if (i == SUCCESS) {
            float[] feature = FaceJNI.string2floatArray(objectInfo.getFeature());
            StarepoSendKafkaObject object = new StarepoSendKafkaObject();
            object.setPkey(objectInfo.getTypeid());
            object.setRowkey(objectInfo.getId());
            if (feature != null && feature.length > 0) {
                ObjectFeature objectFeature = new ObjectFeature();
                objectFeature.setId(objectInfo.getId());
                objectFeature.setTypeId(objectInfo.getTypeid());
                objectFeature.setFeature(feature);
                boolean boo = memoryCache.writeMemory(objectFeature);
                if (boo) {
                    object.setFeature(feature);
                } else {
                    return FAILED;
                }
            }
            starepoKafkaProducer.sendKafkaMessage(
                    StarepoKafkaProducer.TOPIC,
                    StarepoKafkaProducer.ADD,
                    JacksonUtil.toJson(object));
        }
        return i;
    }

    /**
     * 根据id查询对象
     *
     * @param objectId 对象ID
     * @return ObjectInfoVo
     */
    public ObjectInfoVO getObjectInfo(String objectId) {
        ObjectInfo objectInfo = objectInfoMapper.selectObjectById(objectId);
        String typeName = getObjectTypeId(objectInfo.getTypeid());
        return ObjectInfoVO.ObjectInfoShift(objectInfo, typeName);
    }

    /**
     * 根据id查询对象照片
     *
     * @param objectId 对象ID
     * @return byte[] 对象照片
     */
    public byte[] getObjectPhoto(String objectId) {
        ObjectInfo objectInfo = objectInfoMapper.selectObjectById(objectId);
        return objectInfo.getPicture();
    }

    /**
     * 获取特征值
     *
     * @param objectId 对象ID
     * @return PictureData
     */
    public PictureData getFeature(String objectId) {
        PictureData pictureData = new PictureData();
        ObjectInfo objectInfo = objectInfoMapper.selectObjectById(objectId);
        byte[] picture = objectInfo.getPicture();
        if (picture != null) {
            pictureData = restTemplate.postForObject("http://face/extract_bytes", picture, PictureData.class);
        }
        return pictureData;
    }
}
