package com.hzgc.cluster.dispatch.compare;

import com.hzgc.cluster.dispatch.cache.CaptureCache;
import com.hzgc.cluster.dispatch.cache.TableCache;
import com.hzgc.cluster.dispatch.dao.DispatchRecognizeMapper;
import com.hzgc.cluster.dispatch.dao.DispatchWhiteMapper;
import com.hzgc.cluster.dispatch.model.DispatchRecognize;
import com.hzgc.cluster.dispatch.model.DispatchWhite;
import com.hzgc.cluster.dispatch.producer.AlarmMessage;
import com.hzgc.cluster.dispatch.producer.Producer;
import com.hzgc.common.collect.bean.FaceObject;
import com.hzgc.common.collect.util.CollectUrlUtil;
import com.hzgc.common.service.api.bean.CameraQueryDTO;
import com.hzgc.common.service.api.service.PlatformService;
import com.hzgc.common.util.basic.UuidUtil;
import com.hzgc.common.util.json.JacksonUtil;
import com.hzgc.jniface.FaceUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Slf4j
@Component
public class FaceCompareForWhite implements Runnable{
    private boolean action;
    @Autowired
    private CaptureCache captureCache;
    @Autowired
    private TableCache tableCache;
    @Autowired
    private DispatchRecognizeMapper dispatureRecognizeMapper;
    @Autowired
    private DispatchWhiteMapper dispatchWhiteMapper;
    @Autowired
    private PlatformService platformService;
    @Autowired
    private Producer producer;
    @Value("${kafka.topic.dispatch-show}")
    private String topic;

    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public FaceCompareForWhite(){
        action = true;
    }

    @Override
    public void run() {
        while (action){
            long start = System.currentTimeMillis();
            List<FaceObject> faceObjects = captureCache.getFaceObjectsForWhite();
            if(faceObjects.size() == 0){
                log.info("The size of face for white is 0");
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                continue;
            }

            List<String> ipcIds = new ArrayList<>();
            for(FaceObject faceObject : faceObjects){
                ipcIds.add(faceObject.getIpcId());
            }

            Map<String, CameraQueryDTO> map = new HashMap<>();
            try {
                map = platformService.getCameraInfoByBatchIpc(ipcIds);
            }catch (Exception e){
                log.error(e.getMessage());
                e.printStackTrace();
                continue;
            }

            try {
                for (FaceObject faceObject : faceObjects) {
                    if(faceObject.getIpcId() == null){
                        log.error("The ipcId of captch is null");
                        continue;
                    }
                    if(map.get(faceObject.getIpcId()) == null){
                        log.error("There is no region found by deviceId : " + faceObject.getIpcId());
                        continue;
                    }
                    Set<float[]> features = tableCache.getFeatures(faceObject.getIpcId());
                    if (features == null) {
                        continue;
                    }
                    boolean isWhitePoeple = false;
                    for (float[] feature : features) {
                        float sim = FaceUtil.featureCompare(faceObject.getAttribute().getFeature(), feature);
                        if (sim > 70) {
                            isWhitePoeple = true;
                            break;
                        }
                    }
                    if (!isWhitePoeple) {
                        if(tableCache.getWhiteRuleId(faceObject.getIpcId()) == null){
                            log.info("There is no white rule for ipcId " + faceObject.getIpcId());
                            continue;
                        }
                        DispatchRecognize dispatureRecognize = new DispatchRecognize();
                        dispatureRecognize.setId(UuidUtil.getUuid().substring(0, 32));
                        dispatureRecognize.setDispatchId(tableCache.getWhiteRuleId(faceObject.getIpcId()));
                        dispatureRecognize.setDeviceId(faceObject.getIpcId());
                        dispatureRecognize.setType(3);
                        String surl = CollectUrlUtil.toHttpPath(faceObject.getHostname(), "2573", faceObject.getsAbsolutePath());
                        String burl = CollectUrlUtil.toHttpPath(faceObject.getHostname(), "2573", faceObject.getbAbsolutePath());
                        dispatureRecognize.setSurl(surl);
                        dispatureRecognize.setBurl(burl);
                        try {
                            dispatureRecognize.setRecordTime(sdf.parse(faceObject.getTimeStamp()));
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        dispatureRecognize.setCreateTime(new Date());
                        try {
                            dispatureRecognizeMapper.insertSelective(dispatureRecognize);
                        } catch (Exception e) {
                            e.printStackTrace();
                            log.error(e.getMessage());
                        }

                        DispatchWhite dispatchWhite = dispatchWhiteMapper.selectByPrimaryKey(tableCache.getWhiteRuleId(faceObject.getIpcId()));
                        if(dispatchWhite == null){
                            log.error("There is no white rule for id " + tableCache.getWhiteRuleId(faceObject.getIpcId()));
                            continue;
                        }
                        AlarmMessage alarmMessage = new AlarmMessage();
                        String ip = faceObject.getIp();
                        alarmMessage.setBCaptureImage(CollectUrlUtil.toHttpPath(ip, "2573", faceObject.getbAbsolutePath()));
                        alarmMessage.setCaptureImage(CollectUrlUtil.toHttpPath(ip, "2573", faceObject.getsAbsolutePath()));
                        alarmMessage.setDeviceId(faceObject.getIpcId());
                        alarmMessage.setName(dispatchWhite.getName());
                        alarmMessage.setDeviceName(map.get(faceObject.getIpcId()).getCameraName());
                        alarmMessage.setType(3);
                        alarmMessage.setTime(faceObject.getTimeStamp());
                        producer.send(topic, JacksonUtil.toJson(alarmMessage));
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
                log.error(e.getMessage());
            }
            log.info("The size of face compared for white is " + faceObjects.size() + " , the time is " + (System.currentTimeMillis() - start));
        }
    }
}
