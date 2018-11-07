package com.hzgc.service.dispatch.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.hzgc.common.service.api.service.PlatformService;
import com.hzgc.common.service.response.ResponseResult;
import com.github.pagehelper.PageInfo;
import com.hzgc.common.service.api.service.InnerService;
import com.hzgc.common.util.basic.ListUtil;
import com.hzgc.common.util.basic.UuidUtil;
import com.hzgc.common.util.json.JacksonUtil;
import com.hzgc.jniface.FaceAttribute;
import com.hzgc.jniface.FaceUtil;
import com.hzgc.service.dispatch.dao.DispatchMapper;
import com.hzgc.service.dispatch.dao.DispatchRecognizeMapper;
import com.hzgc.service.dispatch.param.*;
import com.hzgc.service.util.DispatchExcelUtils;
import com.hzgc.service.dispatch.model.Dispatch;
import com.hzgc.service.dispatch.model.DispatchRecognize;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotNull;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutionException;

@Service
@Slf4j
public class DispatchService {
    @Autowired
    @SuppressWarnings("unused")
    private DispatchMapper dispatchMapper;

    @Autowired
    @SuppressWarnings("unused")
    private InnerService innerService;

    @Autowired
    @SuppressWarnings("unused")
    private DispatchRecognizeMapper dispatchRecognizeMapper;

    @Autowired
    @SuppressWarnings("unused")
    private PlatformService platformService;

    @Autowired
    @SuppressWarnings("unused")
    //Spring-kafka-template
    private KafkaTemplate<String, String> kafkaTemplate;

    @Value("${dispatch.kafka.topic}")
    @NotNull
    @SuppressWarnings("unused")
    private String kafkaTopic;

    private static final String ADD = "ADD";

    private static final String DELETE = "DELETE";

    private static final String UPDATE = "UPDATE";

    private static final String IMPORT = "IMPORT";

    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private void sendKafka(String key, Object data) {
        kafkaTemplate.send(kafkaTopic, key, JacksonUtil.toJson(data));
    }

    //布控告警历史查询
    public ResponseResult<WarnHistoryVO> searchDeployRecognize(DispatchRecognizeDTO dispatchRecognizeDTO) {
        List<DispatchRecognize> dispatchRecognizeList = dispatchRecognizeMapper.selectSelective(dispatchRecognizeDTO);
        ArrayList<DispatchRecognizeVO> dispatchRecognizeVOS = new ArrayList<>();
        if (null != dispatchRecognizeList && dispatchRecognizeList.size() > 0) {
            for (DispatchRecognize dispatchRecognize : dispatchRecognizeList) {
                DispatchDTO dispatchDTO = new DispatchDTO();
                String dispatchId = dispatchRecognize.getDispatchId();
                dispatchDTO.setId(dispatchId);
                dispatchDTO.setRegionId(dispatchRecognizeDTO.getRegionId());
                Dispatch dispatch = dispatchMapper.selectSelective(dispatchDTO);
                if (null != dispatch) {
                    DispatchRecognizeVO dispatchRecognizeVO = getDispatchRecognizeVO(dispatch, dispatchRecognize);
                    dispatchRecognizeVOS.add(dispatchRecognizeVO);
                }
            }
        }
        WarnHistoryVO warnHistoryVO = new WarnHistoryVO();
        warnHistoryVO.setTotal(dispatchRecognizeVOS.size());
        warnHistoryVO.setDispatchRecognizeVOS(ListUtil.pageSplit(dispatchRecognizeVOS,
                dispatchRecognizeDTO.getStart(), dispatchRecognizeDTO.getLimit()));
        return ResponseResult.init(warnHistoryVO);
    }

    /**
     * 查询布控信息（模糊查询）
     *
     * @param searchDispatchDTO 查询字段封装
     * @return DispatchVO 查询返回参数封装
     */
    public SearchDispatchVO searchDispatch(SearchDispatchDTO searchDispatchDTO) {
        SearchDispatchVO vo = new SearchDispatchVO();
        Page page = PageHelper.offsetPage(searchDispatchDTO.getStart(), searchDispatchDTO.getLimit(), true);
        List<Dispatch> dispatchList = dispatchMapper.searchDispatch(searchDispatchDTO);
        PageInfo info = new PageInfo(page.getResult());
        int total = (int) info.getTotal();
        vo.setTotal(total);
        Map<String, Long> regionMap = platformService.getAllRegionId();
        List<DispatchVO> list = new ArrayList<>();
        for (Dispatch dispatch : dispatchList) {
            DispatchVO dispatchVO = new DispatchVO();
            dispatchVO.setId(dispatch.getId());
            dispatchVO.setRegionId(dispatch.getRegion());
            for (Map.Entry entry : regionMap.entrySet()) {
                if (dispatch.getRegion().equals(entry.getValue())) {
                    dispatchVO.setRegionName((String) entry.getKey());
                }
            }
            dispatchVO.setName(dispatch.getName());
            dispatchVO.setIdCard(dispatch.getIdcard());
            dispatchVO.setThreshold(dispatch.getThreshold());
            dispatchVO.setCar(dispatch.getCar());
            dispatchVO.setMac(dispatch.getMac());
            dispatchVO.setNotes(dispatch.getNotes());
            dispatchVO.setStatus(dispatch.getStatus());
            dispatchVO.setCreateTime(sdf.format(dispatch.getCreateTime()));
            dispatchVO.setUpdateTime(sdf.format(dispatch.getCreateTime()));
            list.add(dispatchVO);
        }
        vo.setPeopleVOList(list);
        return vo;
    }

    /**
     * 根据人员ID查询布控人脸照片
     *
     * @param id 人员ID
     * @return byte[] 照片
     */
    public byte[] getFace(String id) {
        Dispatch dispatch = dispatchMapper.selectFaceById(id);
        if (dispatch != null) {
            return dispatch.getFace();
        }
        return null;
    }

    /**
     * 修改布控人员状态
     *
     * @param id     人员ID
     * @param status 状态
     * @return 0：失败，1：成功
     */
    public Integer dispatchStatus(String id, int status) {
        Dispatch dispatch = new Dispatch();
        dispatch.setId(id);
        dispatch.setStatus(status);
        int i = dispatchMapper.updateStatusById(dispatch);
        if (i == 1) {
            if (status == 0) {
                Dispatch dispatchData = dispatchMapper.selectByPrimaryKey(id);
                KafkaMessage kafkaMessage = new KafkaMessage();
                kafkaMessage.setId(id);
                kafkaMessage.setRegionId(dispatchData.getRegion());
                kafkaMessage.setBitFeature(dispatchData.getBitFeature());
                kafkaMessage.setCar(dispatchData.getCar());
                kafkaMessage.setMac(dispatchData.getMac());
                this.sendKafka(ADD, kafkaMessage);
            }
            if (status == 1) {
                this.sendKafka(DELETE, id);
            }
            return 1;
        }
        return 0;
    }

    public Integer insertDeploy(DispatchDTO dto) {
        Dispatch dispatch = new Dispatch();
        dispatch.setId(UuidUtil.getUuid());
        dispatch.setRegion(dto.getRegionId());
        dispatch.setName(dto.getName());
        dispatch.setIdcard(dto.getIdCard());
        dispatch.setCar(dto.getCar());
        dispatch.setMac(dto.getMac());
        dispatch.setNotes(dto.getNotes());
        if (dto.getFace() != null) {
            byte[] bytes = FaceUtil.base64Str2BitFeature(dto.getFace());
            FaceAttribute faceAttribute =
                    innerService.faceFeautreExtract(dto.getFace()) != null ? innerService.faceFeautreExtract(dto.getFace()).getFeature() : null;
            if (faceAttribute == null || faceAttribute.getFeature() == null || faceAttribute.getBitFeature() == null) {
                log.error("Face feature extract failed, insert t_dispatch failed");
                throw new RuntimeException("Face feature extract failed, insert  t_dispatch failed");
            }
            dispatch.setFace(bytes);
            dispatch.setFeature(FaceUtil.floatFeature2Base64Str(faceAttribute.getFeature()));
            dispatch.setBitFeature(FaceUtil.bitFeautre2Base64Str(faceAttribute.getBitFeature()));
        }
        Integer status = dispatchMapper.insertSelective(dispatch);
        if (status != 1) {
            log.error("Insert info failed");
            return 0;
        }
        KafkaMessage message = new KafkaMessage();
        message.setId(dispatch.getId());
        message.setRegionId(dispatch.getRegion());
        message.setBitFeature(dispatch.getBitFeature());
        message.setCar(dispatch.getCar());
        message.setMac(dispatch.getMac());
        this.sendKafka(ADD, message);
        log.info("Insert info successfully");
        return status;
    }

    public Integer deleteDeploy(String id) {
        Integer status = dispatchMapper.deleteByPrimaryKey(id);
        if (status != 1) {
            log.info("Delete info failed ");
            return 0;
        }
        this.sendKafka(DELETE, id);
        log.info("Delete info successfully ");
        return status;
    }

    public Integer updateDeploy(DispatchDTO dto) {
        Dispatch dispatch = new Dispatch();
        dispatch.setId(dto.getId());
        dispatch.setRegion(dto.getRegionId());
        dispatch.setName(dto.getName());
        dispatch.setIdcard(dto.getIdCard());
        dispatch.setCar(dto.getCar());
        dispatch.setMac(dto.getMac());
        dispatch.setNotes(dto.getNotes());
        if (dto.getFace() != null) {
            byte[] bytes = FaceUtil.base64Str2BitFeature(dto.getFace());
            FaceAttribute faceAttribute = innerService.faceFeautreExtract(dto.getFace()).getFeature();
            if (faceAttribute == null || faceAttribute.getFeature() == null || faceAttribute.getBitFeature() == null) {
                log.error("Face feature extract failed, update t_dispatch failed");
                throw new RuntimeException("Face feature extract failed, update t_dispatch failed");
            }
            dispatch.setFace(bytes);
            dispatch.setFeature(FaceUtil.floatFeature2Base64Str(faceAttribute.getFeature()));
            dispatch.setBitFeature(FaceUtil.bitFeautre2Base64Str(faceAttribute.getBitFeature()));
        }
        Integer status = dispatchMapper.updateByPrimaryKeySelective(dispatch);
        if (status != 1) {
            log.error("update info failed");
            return 0;
        }
        KafkaMessage message = new KafkaMessage();
        message.setId(dispatch.getId());
        message.setRegionId(dispatch.getRegion());
        message.setBitFeature(dispatch.getBitFeature());
        message.setCar(dispatch.getCar());
        message.setMac(dispatch.getMac());
        this.sendKafka(UPDATE, message);
        log.info("update info successfully");
        return status;
    }

    //DispatchRecognizeVO 数据封装
    private DispatchRecognizeVO getDispatchRecognizeVO(Dispatch dispatch, DispatchRecognize dispatchRecognize) {
        DispatchRecognizeVO dispatchRecognizeVO = new DispatchRecognizeVO();
        dispatchRecognizeVO.setId(dispatchRecognize.getId());
        dispatchRecognizeVO.setDispatchId(dispatchRecognize.getDispatchId());
        dispatchRecognizeVO.setDeviceId(dispatchRecognize.getDeviceId());
        dispatchRecognizeVO.setBurl(dispatchRecognize.getBurl());
        dispatchRecognizeVO.setSurl(dispatchRecognize.getSurl());
        dispatchRecognizeVO.setSimilarity(dispatchRecognize.getSimilarity());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        dispatchRecognizeVO.setRecordTime(sdf.format(dispatchRecognize.getRecordTime()));
        dispatchRecognizeVO.setDeviceName(getDeviceName(dispatchRecognize.getDeviceId()));
        dispatchRecognizeVO.setType(dispatchRecognize.getType());
        dispatchRecognizeVO.setName(dispatch.getName());
        dispatchRecognizeVO.setIdCard(dispatch.getIdcard());
        dispatchRecognizeVO.setCar(dispatch.getCar());
        dispatchRecognizeVO.setMac(dispatch.getMac());
        dispatchRecognizeVO.setNotes(dispatch.getNotes());
        return dispatchRecognizeVO;
    }

    //分页
    private List<DispatchRecognizeVO> getDispatchRecognizeVOByCutPage(DispatchRecognizeDTO dispatchRecognizeDTO,
                                                                      List<DispatchRecognizeVO> dispatchRecognizeVOS) {
        int start = dispatchRecognizeDTO.getStart();
        int limit = dispatchRecognizeDTO.getLimit();
        if (null != dispatchRecognizeVOS && dispatchRecognizeVOS.size() > 0) {
            if (dispatchRecognizeVOS.size() > start && dispatchRecognizeVOS.size() < (start + limit)) {
                return dispatchRecognizeVOS.subList(start, dispatchRecognizeVOS.size());
            }
            if (dispatchRecognizeVOS.size() > start && dispatchRecognizeVOS.size() >= (start + limit)) {
                return dispatchRecognizeVOS.subList(start, limit);
            }
            return null;
        }
        return null;
    }

    //查询外部接口(获取相机名称)
    private String getDeviceName(String deviceId) {
        String cameraDeviceName = platformService.getCameraDeviceName(deviceId);
        return cameraDeviceName;
    }

    public Integer excelImport(MultipartFile file) {
        DispatchExcelUtils excelUtils = new DispatchExcelUtils(file);
        Map<Integer, Map<Integer, Object>> excelMap = null;
        try {
            excelMap = excelUtils.readExcelContent();
        } catch (Exception e) {
            log.error("Import excel data failed, because read excel error");
            e.printStackTrace();
        }
        if (excelMap == null || excelMap.size() == 0) {
            return 0;
        }
        Map<String, Long> regionMap = platformService.getAllRegionId();
        Set<String> keys = regionMap.keySet();
        List<String> regionNames = new ArrayList<>(keys);
        List<Dispatch> dispatchList = new ArrayList<>();
        for (int i = 1; i <= excelMap.size(); i++) {
            Map<Integer, Object> map = excelMap.get(i);
            Dispatch dispatch = new Dispatch();
            if (map.get(0) != null && !"".equals(map.get(0))
                    && regionNames.contains(String.valueOf(map.get(0)))) {
                dispatch.setRegion(regionMap.get(String.valueOf(map.get(0))));
            } else {
                log.error("Region is error, please check line: " + i);
                return 0;
            }
            if (map.get(1) != null && !"".equals(map.get(1))) {
                dispatch.setName((String) map.get(1));
            }
            if (map.get(2) != null && !"".equals(map.get(2))) {
                if (DispatchExcelUtils.isCarNumber(String.valueOf(map.get(2)))) {
                    dispatch.setCar(String.valueOf(map.get(2)));
                } else {
                    log.error("Car is error, please check line: " + i);
                    return 0;
                }
            }
            if (map.get(3) != null && !"".equals(map.get(3))) {
                if (DispatchExcelUtils.isMac(String.valueOf(map.get(3)))) {
                    dispatch.setMac(String.valueOf(map.get(3)));
                } else {
                    log.error("Mac is error, please check line: " + i);
                    return 0;
                }
            }
            if (map.get(4) != null && !"".equals(map.get(4))) {
                if (DispatchExcelUtils.isIdCard(String.valueOf(map.get(4)))) {
                    dispatch.setIdcard(String.valueOf(map.get(4)));
                } else {
                    log.error("Idcard is error, please check line: " + i);
                    return 0;
                }
            }
            if (map.get(5) != null && !"".equals(map.get(5))) {
                if (DispatchExcelUtils.isThreshold(String.valueOf(map.get(5)))) {
                    dispatch.setThreshold(Float.valueOf(String.valueOf(map.get(5))));
                } else {
                    log.error("Threshold is error, please check line: " + i);
                    return 0;
                }
            }
            if (map.get(6) != null && !"".equals(map.get(6))) {
                dispatch.setNotes(String.valueOf(map.get(6)));
            }
            dispatchList.add(dispatch);
        }
        log.info("Excel data conversion is completed, start insert into t_dispatch table");
        Integer status = this.excelImport(dispatchList);
        if (status != 1) {
            log.error("Import excel data failed, because insert into t_dispatch table failed");
            return 0;
        }
        return 1;
    }

    @Transactional(rollbackFor = Exception.class)
    private Integer excelImport(List<Dispatch> dispatchList) {
        for (Dispatch dispatch : dispatchList) {
            dispatch.setId(UuidUtil.getUuid());
            int status = dispatchMapper.insertSelective(dispatch);
            if (status != 1) {
                throw new RuntimeException("Insert into t_dispatch table failed");
            }
            if (StringUtils.isNotBlank(dispatch.getCar()) && StringUtils.isNotBlank(dispatch.getMac())) {
                KafkaMessage message = new KafkaMessage();
                message.setId(dispatch.getId());
                message.setRegionId(dispatch.getRegion());
                message.setCar(dispatch.getCar());
                message.setMac(dispatch.getMac());
                this.sendKafka(ADD, message);
            }
        }
        return 1;
    }

}
