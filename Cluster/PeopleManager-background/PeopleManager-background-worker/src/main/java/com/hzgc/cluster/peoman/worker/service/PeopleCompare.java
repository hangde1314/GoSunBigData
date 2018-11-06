package com.hzgc.cluster.peoman.worker.service;

import com.google.gson.Gson;
import com.hzgc.cluster.peoman.worker.dao.FlagMapper;
import com.hzgc.cluster.peoman.worker.dao.PeopleMapper;
import com.hzgc.cluster.peoman.worker.dao.PeopleRecognizeMapper;
import com.hzgc.cluster.peoman.worker.model.People;
import com.hzgc.cluster.peoman.worker.model.PeopleRecognize;
import com.hzgc.common.collect.bean.FaceObject;
import com.hzgc.common.collect.util.CollectUrlUtil;
import com.hzgc.common.service.api.bean.CameraQueryDTO;
import com.hzgc.common.service.api.service.PlatformService;
import com.hzgc.common.util.json.JacksonUtil;
import com.hzgc.common.util.rocketmq.RocketMQProducer;
import com.hzgc.jniface.CompareResult;
import com.hzgc.jniface.FaceFeatureInfo;
import com.hzgc.jniface.FaceFunction;
import com.hzgc.jniface.FaceUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.rocketmq.client.producer.SendResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Component
@Slf4j
public class PeopleCompare {
    @Autowired
    @SuppressWarnings("unused")
    private PlatformService platformService;

    @Autowired
    @SuppressWarnings("unused")
    private MemeoryCache memeoryCache;

    @Autowired
    @SuppressWarnings("unused")
    private PeopleMapper peopleMapper;

    @Autowired
    @SuppressWarnings("unused")
    private PeopleRecognizeMapper peopleRecognizeMapper;

    @Autowired
    @SuppressWarnings("unused")
    private FlagMapper flagMapper;

    @Value("${kafka.fusion.topic}")
    @SuppressWarnings("unused")
    private String fusionTopic;

    @Value("${face.float.threshold}")
    @SuppressWarnings("unused")
    private float floatThreshold;

    @Value("${face.bit.threshold}")
    @SuppressWarnings("unused")
    private float featureThreshold;

    @Value("${filter.interval.time}")
    @SuppressWarnings("unused")
    private int filterTime;

    @Value("${face.float.compare.open}")
    @SuppressWarnings("unused")
    private boolean isOpen;

    @Value("${rocketmq.nameserver}")
    @SuppressWarnings("unused")
    private String mqNameServer;

    @Value("${rocketmq.topic.name}")
    @SuppressWarnings("unused")
    private String mqTopicName;

    @Value("${rocketmq.group.id}")
    @SuppressWarnings("unused")
    private String mqGroupId;

    private String yearMonth = "";
    private Map<Integer, String> indexUUID = new HashMap<>();
    private Map<String, CameraQueryDTO> cameraInfos = new HashMap<>();
    private LinkedList<byte[]> bitFeatureList = new LinkedList<>();
    private LinkedList<float[]> floatFeatureList = new LinkedList<>();
    private KafkaProducer<String, String> producer;

    public PeopleCompare(@Value("${kafka.bootstrap.servers}") String kafkaHost) {
        FaceFunction.init();
        Properties properties = new Properties();
        properties.put("bootstrap.servers", kafkaHost);
        properties.put("acks", "all");
        properties.put("retries", 0);
        properties.put("batch.size", 0);
        properties.put("linger.ms", 1);
        properties.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        properties.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        producer = new KafkaProducer<String, String>(properties);
    }

    public void comparePeople(FaceObject faceObject) {
        String currentYearMonth = new SimpleDateFormat("yyyyMM").format(System.currentTimeMillis());
        if (!currentYearMonth.equals(yearMonth)) {
            log.info("PeopleComare init, currentYearMonth=" + currentYearMonth + ", yearMonth=" + yearMonth);
            bitFeatureList.clear();
            floatFeatureList.clear();
            indexUUID.clear();
            yearMonth = currentYearMonth;
        }
        if(cameraInfos.size() <= 0) {
            Map<String, CameraQueryDTO> cameraInfoByBatchIpc = platformService.getCameraInfoByBatchIpc(new ArrayList<>());
            if (cameraInfoByBatchIpc != null && cameraInfoByBatchIpc.size() > 0) {
                cameraInfos.putAll(cameraInfoByBatchIpc);
            } else {
                log.error("platformService getCameraInfoByBatchIpc all data is null or empty exit !!!");
                return;
            }
        }
        if(cameraInfos == null || cameraInfos.size() == 0) {
            log.error("PeopleCompare cameraInfos data is null or empty exit !!!");
            return;
        }

        CameraQueryDTO cameraInfo = cameraInfos.get(faceObject.getIpcId());
        Long communityId = null;
        if (cameraInfo == null) {
            ArrayList<String> list = new ArrayList<>();
            list.add(faceObject.getIpcId());
            Map<String, CameraQueryDTO> cameraInfoByIpc = platformService.getCameraInfoByBatchIpc(list);
            if (cameraInfoByIpc != null && cameraInfoByIpc.size() > 0) {
                CameraQueryDTO cameraIpc = cameraInfoByIpc.get(faceObject.getIpcId());
                if(cameraIpc != null) {
                    communityId = cameraIpc.getCommunityId();
                    cameraInfos.put(faceObject.getIpcId(), cameraIpc);
                } else {
                    log.error("cameraIpc data no community exit !!!, devId="+faceObject.getIpcId());
                    return;
                }
            } else {
                log.error("platformService getCameraInfoByBatchIpc data no community exit !!!, devId="+faceObject.getIpcId());
                return;
            }
        } else {
            communityId = cameraInfo.getCommunityId();
        }
        ComparePicture comparePicture = memeoryCache.comparePicture(faceObject.getAttribute());
        if (comparePicture != null) {
            addPeopleRecognize(faceObject, comparePicture, communityId);

            //重点人口推送
            if (comparePicture.getFlagId() != null && comparePicture.getFlagId() != 7) {
                MessageMq mesg = new MessageMq();
                Gson gson = new Gson();
                mesg.setName(comparePicture.getName());
                mesg.setTime(faceObject.getTimeStamp());
                mesg.setDevId(faceObject.getIpcId());
                RocketMQProducer producerMQ = RocketMQProducer.getInstance(mqNameServer, mqTopicName, mqGroupId);
                SendResult sendResult = producerMQ.send(mqTopicName, "ZD-Message", comparePicture.getPeopleId(), gson.toJson(mesg).getBytes(), null);
            }
            ProducerRecord<String, String> record = new ProducerRecord<>(fusionTopic, faceObject.getId(), JacksonUtil.toJson(faceObject));
            producer.send(record);
        } else {
            addNewPeopleRecognize(faceObject, communityId);
        }
    }

    public void addPeopleRecognize(FaceObject faceObject, ComparePicture comparePicture, Long communityId) {
        Date date = null;
        try {
            date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(faceObject.getTimeStamp());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        People people = new People();
        people.setId(comparePicture.getPeopleId());
        people.setLasttime(date);
        peopleMapper.updateByPrimaryKeySelective(people);

        PeopleRecognize peopleRecognize = new PeopleRecognize();
        peopleRecognize.setId(faceObject.getId());
        peopleRecognize.setPeopleid(comparePicture.getPeopleId());
        peopleRecognize.setPictureid(comparePicture.getId());
        peopleRecognize.setCommunity(communityId);
        peopleRecognize.setDeviceid(faceObject.getIpcId());
        peopleRecognize.setCapturetime(date);
        peopleRecognize.setSurl(CollectUrlUtil.toHttpPath(faceObject.getHostname(), "2573", faceObject.getsAbsolutePath()));
        peopleRecognize.setBurl(CollectUrlUtil.toHttpPath(faceObject.getHostname(), "2573", faceObject.getbAbsolutePath()));
        peopleRecognize.setFlag(1);
        peopleRecognize.setSimilarity(comparePicture.getSimilarity());
        peopleRecognize.setFilterTime(filterTime);
        log.info("insert people recognize value=" + JacksonUtil.toJson(peopleRecognize));
        try {
            peopleRecognizeMapper.insertUpdate(peopleRecognize);
        } catch (Exception e) {
            log.info("PeopelCompare insertUpdate people recognize failed !!!");
            log.error(e.getMessage());
        }
    }

    public void addNewPeopleRecognize(FaceObject faceObject, Long communityId) {
        CompareRes compareRes = compareNewPeople(faceObject);
        PeopleRecognize peopleRecognize = new PeopleRecognize();
        Date date = null;
        try {
            date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(faceObject.getTimeStamp());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (compareRes != null) {
            peopleRecognize.setId(faceObject.getId());
            peopleRecognize.setPeopleid(indexUUID.get(compareRes.getIndex()));
            peopleRecognize.setDeviceid(faceObject.getIpcId());
            peopleRecognize.setPictureid(-1L);
            peopleRecognize.setCapturetime(date);
            peopleRecognize.setSurl(CollectUrlUtil.toHttpPath(faceObject.getHostname(), "2573", faceObject.getsAbsolutePath()));
            peopleRecognize.setBurl(CollectUrlUtil.toHttpPath(faceObject.getHostname(), "2573", faceObject.getbAbsolutePath()));
            peopleRecognize.setCommunity(communityId);
            peopleRecognize.setFlag(compareRes.getFlag());
            peopleRecognize.setSimilarity(compareRes.getSimilarity());
            peopleRecognize.setFilterTime(filterTime);
            log.info("insert new add people recognize value=" + JacksonUtil.toJson(peopleRecognize));
            if(compareRes.getFlag() == 10) {
                try {
                    peopleRecognizeMapper.insert(peopleRecognize);
                } catch (Exception e) {
                    bitFeatureList.removeLast();
                    floatFeatureList.removeLast();
                    indexUUID.remove(indexUUID.size() -1);
                    log.info("PeopelCompare flag=10 insert new add people recognize failed !!!");
                    log.error(e.getMessage());
                }
            } else {
                try {
                    peopleRecognizeMapper.insert(peopleRecognize);
                } catch (Exception e) {
                    log.info("PeopelCompare flag=2 insert new add people recognize failed !!!");
                    log.error(e.getMessage());
                }
            }
        }
    }

    /**
     * 新增人口比对
     *
     * @param faceObject
     * @return index : 集合下标
     * flag : 识别标签(2 : 新增, 10 ： 完全新增(原图))
     */
    public CompareRes compareNewPeople(FaceObject faceObject) {
        CompareRes compareRes = new CompareRes();
        byte[] bitFeature = faceObject.getAttribute().getBitFeature();
        if (bitFeature != null && bitFeature.length > 0) {
            byte[][] queryList = new byte[1][];
            queryList[0] = bitFeature;
            ArrayList<CompareResult> compareResList =
                    FaceFunction.faceCompareBit(bitFeatureList.toArray(new byte[0][]), queryList, 1);
            if (compareResList == null) {
                bitFeatureList.addLast(faceObject.getAttribute().getBitFeature());
                floatFeatureList.addLast(faceObject.getAttribute().getFeature());
                indexUUID.put(indexUUID.size(), faceObject.getId());
                compareRes.setFlag(10);
                compareRes.setIndex(indexUUID.size() -1);
                compareRes.setSimilarity(Float.valueOf("0.0"));
                log.info("----------CompareNewPeople flag=10 compareResList is null");
                return compareRes;
            }
            CompareResult compareResult = compareResList.get(0);
            ArrayList<FaceFeatureInfo> featureInfos = compareResult.getPictureInfoArrayList();
            FaceFeatureInfo faceFeatureInfo = featureInfos.get(0);
            if (isOpen && (faceFeatureInfo.getScore() >= featureThreshold / 100.0)) {
                float[] floatFeature = floatFeatureList.get(faceFeatureInfo.getIndex());
                if (floatFeature != null && floatFeature.length == 512 && faceObject.getAttribute().getFeature().length == 512) {
                    float sim = FaceUtil.featureCompare(floatFeature, faceObject.getAttribute().getFeature());
                    if (sim >= this.floatThreshold) {
                        compareRes.setFlag(2);
                        compareRes.setIndex(faceFeatureInfo.getIndex());
                        compareRes.setSimilarity(sim);
                        log.info("----------CompareNewPeople flag=2 Float Sim="+sim);
                        return compareRes;
                    } else {
                        bitFeatureList.addLast(faceObject.getAttribute().getBitFeature());
                        indexUUID.put(indexUUID.size(), faceObject.getId());
                        floatFeatureList.addLast(faceObject.getAttribute().getFeature());
                        compareRes.setFlag(10);
                        compareRes.setIndex(indexUUID.size() -1);
                        compareRes.setSimilarity(sim);
                        log.info("----------CompareNewPeople flag=10 Float Sim="+sim);
                        return compareRes;
                    }
                } else {
                    return null;
                }

            } else {
                if (faceFeatureInfo.getScore() >= featureThreshold / 100) {
                    compareRes.setFlag(2);
                    compareRes.setIndex(Integer.valueOf(compareResult.getIndex()));
                    compareRes.setSimilarity(faceFeatureInfo.getScore()*100);
                    log.info("----------CompareNewPeople flag=2 Bit Score="+faceFeatureInfo.getScore());
                    return compareRes;
                } else {
                    floatFeatureList.addLast(faceObject.getAttribute().getFeature());
                    bitFeatureList.addLast(faceObject.getAttribute().getBitFeature());
                    indexUUID.put(indexUUID.size(), faceObject.getId());
                    compareRes.setFlag(10);
                    compareRes.setIndex(indexUUID.size() -1);
                    compareRes.setSimilarity(faceFeatureInfo.getScore()*100);
                    log.info("----------CompareNewPeople flag=10 Bit Score="+faceFeatureInfo.getScore());
                    return compareRes;
                }
            }
        } else {
            return null;
        }
    }
}
