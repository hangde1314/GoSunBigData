package com.hzgc.service.alive.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.hzgc.common.service.api.service.PlatformService;
import com.hzgc.common.util.basic.UuidUtil;
import com.hzgc.common.util.json.JacksonUtil;
import com.hzgc.service.alive.dao.AliveMapper;
import com.hzgc.service.alive.model.Alive;
import com.hzgc.service.alive.param.AliveInfo;
import com.hzgc.service.alive.param.AliveInfoDTO;
import com.hzgc.service.alive.param.AliveInfoVO;
import com.hzgc.service.alive.param.SearchAliveInfoDTO;
import com.hzgc.service.dispatch.param.KafkaMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
@Service
@Slf4j
public class AliveService {
    @Autowired
    private AliveMapper aliveMapper;

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

    private SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");

    private void sendKafka(String key, Object data) {
        kafkaTemplate.send(TOPIC, key, JacksonUtil.toJson(data));
    }

    public Integer insertAliveInfo(AliveInfoDTO dto) {
        Alive alive = new Alive();
        alive.setId(UuidUtil.getUuid());
        alive.setName(dto.getName());
        alive.setDevices(StringUtils.join(dto.getDeviceIds().toArray(), ","));
        alive.setOrganization(dto.getOrganization());
        alive.setStartTime(dto.getStarttime());
        alive.setEndTime(dto.getEndtime());
        int status = aliveMapper.insertSelective(alive);
        if (status != 1) {
            log.info("Insert info,but insert info to t_dispatch_white failed");
            return 0;
        }
        KafkaMessage message = new KafkaMessage();
        message.setId(alive.getId());
        this.sendKafka(ADD, message);
        log.info("Insert info successfully");
        return 1;
    }

    public Integer updateAliveInfo(AliveInfoDTO dto) {
        Alive alive = new Alive();
        alive.setId(dto.getId());
        alive.setName(dto.getName());
        alive.setDevices(StringUtils.join(dto.getDeviceIds().toArray(), ","));
        alive.setOrganization(dto.getOrganization());
        alive.setStartTime(dto.getStarttime());
        alive.setStatus(0);
        alive.setEndTime(dto.getEndtime());
        alive.setCreateTime(new Date());
        int status = aliveMapper.updateByPrimaryKeyWithBLOBs(alive);
        if (status != 1) {
            log.info("Insert info,but insert info to t_dispatch_alive failed");
            return 0;
        }
        KafkaMessage message = new KafkaMessage();
        message.setId(alive.getId());
        this.sendKafka(UPDATE, message);
        log.info("update info successfully");
        return 1;
    }

    public Integer deleteAliveInfo(String id) {
        int status = aliveMapper.deleteByPrimaryKey(id);
        if (status != 1){
            log.info("Delete info failed ");
            return 0;
        }
        this.sendKafka(DELETE, id);
        log.info("Delete info successfully ");
        return status;
    }

    public Integer updateAliveInfoStatus(String id, int status) {
        Alive  alive = new Alive();
        alive.setId(id);
        alive.setStatus(status);
        int i = aliveMapper.updateByPrimaryKeySelective(alive);
        if (i == 1) {
            if (status == 0) {
                this.sendKafka(ADD, id);
            }
            if (status == 1) {
                this.sendKafka(DELETE, id);
            }
            return 1;
        }
        return 0;
    }

    public AliveInfoVO searchAliveInfo(SearchAliveInfoDTO dto) {
        AliveInfoVO vo = new AliveInfoVO();
        Page page = PageHelper.offsetPage(dto.getStart(),dto.getLimit(), true);
        List<Alive> aliveList = aliveMapper.searchAliveInfo(dto.getName());
        PageInfo info = new PageInfo(page.getResult());
        int total = (int) info.getTotal();
        vo.setTotal(total);
        List<AliveInfo> list = new ArrayList<>();
        for (Alive alive : aliveList){
            AliveInfo aliveInfo = new AliveInfo();
            aliveInfo.setId(alive.getId());
            aliveInfo.setName(alive.getName());
            List<String> deviceIds = Arrays.asList(alive.getDevices().split(","));
            aliveInfo.setDeviceIds(deviceIds);
            List<String> deviceNames = new ArrayList<>();
            for (String deviceId : deviceIds){
                String deviceName = platformService.getCameraDeviceName(deviceId);
                deviceNames.add(deviceName);
            }
            aliveInfo.setDeviceNames(deviceNames);
            aliveInfo.setOrganization(alive.getOrganization());
            aliveInfo.setStartTime(alive.getStartTime());
            aliveInfo.setEndTime(alive.getEndTime());
            aliveInfo.setStatus(alive.getStatus());
            aliveInfo.setCreatetime(sdf.format(alive.getCreateTime()));
            list.add(aliveInfo);
        }
        vo.setAliveInfoVOS(list);
        System.out.println(JacksonUtil.toJson(vo)+"======================");
        return vo;
    }



}
