package com.hzgc.service.white.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.hzgc.common.service.api.service.InnerService;
import com.hzgc.common.service.api.service.PlatformService;
import com.hzgc.common.util.basic.UuidUtil;
import com.hzgc.common.util.json.JacksonUtil;
import com.hzgc.jniface.BigPictureData;
import com.hzgc.jniface.FaceAttribute;
import com.hzgc.jniface.FaceUtil;
import com.hzgc.jniface.PictureData;
import com.hzgc.seemmo.util.BASE64Util;
import com.hzgc.service.dispatch.param.KafkaMessage;
import com.hzgc.service.white.dao.WhiteInfoMapper;
import com.hzgc.service.white.dao.WhiteMapper;
import com.hzgc.service.white.model.White;
import com.hzgc.service.white.model.WhiteInfo;
import com.hzgc.service.white.param.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.concurrent.ListenableFuture;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
@Slf4j
public class WhiteService {
    @Autowired
    @SuppressWarnings("unused")
    private WhiteInfoMapper whiteInfoMapper;

    @Autowired
    @SuppressWarnings("unused")
    private WhiteMapper whiteMapper;

    @Autowired
    @SuppressWarnings("unused")
    private InnerService innerService;

    @Autowired
    @SuppressWarnings("unused")
    private PlatformService platformService;

    @Autowired
    @SuppressWarnings("unused")
    private KafkaTemplate<String, String> kafkaTemplate;

    private static final String TOPIC = "dispatch";

    private static final String ADD = "ADD";

    private static final String DELETE = "DELETE";

    private static final String UPDATE = "UPDATE";

    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private void sendKafka(String key, Object data) {
        kafkaTemplate.send(TOPIC, key, JacksonUtil.toJson(data));
    }

    @Transactional(rollbackFor = Exception.class)
    public Integer insertWhiteInfo(WhiteDTO dto) {
        White white = new White();
        if (StringUtils.isBlank(dto.getId())){
            white.setId(UuidUtil.getUuid());
        } else {
            white.setId(dto.getId());
        }
        white.setName(dto.getName());
        white.setDevices(StringUtils.join(dto.getDeviceIds().toArray(), ","));
        white.setOrganization(dto.getOrganization());
        int status = whiteMapper.insertSelective(white);
        if (status != 1) {
            log.info("Insert white info, but insert into t_dispatch_white failed");
            return 0;
        }
        for (PeopleInfo people : dto.getPeopleInfos()) {
            WhiteInfo whiteInfo = new WhiteInfo();
            whiteInfo.setWhiteId(white.getId());
            whiteInfo.setName(people.getName());
            if (people.getPicture() != null) {
                byte[] bytes = FaceUtil.base64Str2BitFeature(people.getPicture());
                System.out.println(people.getPicture());
                FaceAttribute faceAttribute = innerService.faceFeautreCheck(people.getPicture()).getFeature();
                if (faceAttribute == null || faceAttribute.getFeature() == null || faceAttribute.getBitFeature() == null) {
                    log.error("Face feature extract failed, insert t_dispatch_white failed");
                    throw new RuntimeException("Face feature extract failed, insert t_dispatch_white failed");
                }
                whiteInfo.setPicture(bytes);
                whiteInfo.setFeature(FaceUtil.floatFeature2Base64Str(faceAttribute.getFeature()));
                whiteInfo.setBitFeature(FaceUtil.bitFeautre2Base64Str(faceAttribute.getBitFeature()));
            }else {
                log.error("Param error, picture is not null");
                return 0;
            }
            int insertstatus = whiteInfoMapper.insertSelective(whiteInfo);
            if (insertstatus != 1) {
                log.info("Insert white info, but insert into t_dispatch_whiteinfo failed");
                return 0;
            }
            this.sendKafka(ADD, white.getId());
        }
        return 1;
    }

    public Integer deleteWhiteInfo(String id) {
        int status = whiteMapper.deleteByPrimaryKey(id);
        if (status != 1){
            log.info("Delete info failed");
            return 0;
        }
        this.sendKafka(DELETE, id);
        return status;
    }
    @Transactional(rollbackFor = Exception.class)
    public Integer updateWhiteInfo(WhiteDTO dto) {
        int status_delete = whiteMapper.deleteByPrimaryKey(dto.getId());
        int delete_whiteInfo = whiteInfoMapper.deleteInfo(dto.getId());
        System.out.println((dto.getId())+"++++++++++++++===========");
        if (status_delete != 1){
            log.info("Delete t_dispatch_white failed, id is:" + dto.getId());
            return 0;
        }
        int status = this.insertWhiteInfo(dto);
        if (status != 1){
            log.info("Update failed");
            return 0;
        }
        this.sendKafka(UPDATE, dto.getId());
        return status;
    }

    public Integer updateWhiteStatus(String id, int status) {
        White white = new White();
        white.setId(id);
        white.setStatus(status);
        int i = whiteMapper.updateByPrimaryKeySelective(white);
        if (i != 1){
            log.info("Update white status failed");
            return 0;
        }
        if (status == 0) {
            this.sendKafka(ADD, id);
        }
        if (status == 1) {
            this.sendKafka(DELETE, id);
        }
        return 1;
    }

    public SearchWhiteVO searchWhiteInfo(SearchWhiteDTO dto) {
        SearchWhiteVO vo = new SearchWhiteVO();
        Page page = PageHelper.offsetPage(dto.getStart(), dto.getLimit(), true);
        List<White> whiteList = whiteMapper.searchWhiteInfo(dto);
        PageInfo info = new PageInfo(page.getResult());
        int total = (int) info.getTotal();
        vo.setTotal(total);
        List<WhiteVO> dispatchWhiteVOS = new ArrayList<>();
        for(White white: whiteList){
            WhiteVO whiteVO = new WhiteVO();
            whiteVO.setId(white.getId());
            whiteVO.setName(white.getName());
            List<String> deviceIds = Arrays.asList(white.getDevices().split(","));
            whiteVO.setDeviceIds(deviceIds);
            List<String> deviceNames = new ArrayList<>();
            for (String deviceId : deviceIds){
                String deviceName = platformService.getCameraDeviceName(deviceId);
                deviceNames.add(deviceName);
            }
            whiteVO.setDeviceNames(deviceNames);
            whiteVO.setOrganization(white.getOrganization());
            whiteVO.setStatus(white.getStatus());
            List<WhiteInfo> whiteInfoList = whiteInfoMapper.selectByWhiteId(white.getId());
            List<WhiteInfoVO> whiteInfoVOS = new ArrayList<>();
            for (WhiteInfo whiteInfo: whiteInfoList){
                WhiteInfoVO infoVO = new WhiteInfoVO();
                infoVO.setId(whiteInfo.getId());
                infoVO.setWhiteId(whiteInfo.getWhiteId());
                infoVO.setName(whiteInfo.getName());
                whiteInfoVOS.add(infoVO);
            }
            whiteVO.setWhiteInfoVOS(whiteInfoVOS);
            dispatchWhiteVOS.add(whiteVO);
        }
        vo.setWhiteVOS(dispatchWhiteVOS);
        System.out.println(JacksonUtil.toJson(vo)+"++++++++++++++++=================");
        return vo;
    }

    public byte[] getPicture(Long id) {
        WhiteInfo whiteInfo = whiteInfoMapper.selectPictureById(id);
        if (whiteInfo != null){
            return whiteInfo.getPicture();
        }
        return null;
    }
}
