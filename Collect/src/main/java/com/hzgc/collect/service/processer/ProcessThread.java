package com.hzgc.collect.service.processer;

import com.hzgc.collect.config.CollectContext;
import com.hzgc.collect.service.parser.Parser;
import com.hzgc.collect.service.receiver.Event;
import com.hzgc.common.collect.bean.CarObject;
import com.hzgc.common.collect.bean.FaceObject;
import com.hzgc.common.collect.bean.PersonObject;
import com.hzgc.common.collect.facesub.FtpSubscribeClient;
import com.hzgc.common.util.basic.FileUtil;
import com.hzgc.common.util.basic.ImageUtil;
import com.hzgc.common.util.basic.UuidUtil;
import com.hzgc.common.util.json.JacksonUtil;
import com.hzgc.jniface.FaceAttribute;
import com.hzgc.jniface.FaceJNI;
import com.hzgc.jniface.PictureFormat;
import com.hzgc.jniface.SmallImage;
import com.hzgc.seemmo.bean.ImageResult;
import com.hzgc.seemmo.bean.carbean.Vehicle;
import com.hzgc.seemmo.bean.personbean.Person;
import com.hzgc.seemmo.service.ImageToData;
import lombok.extern.slf4j.Slf4j;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

@Slf4j
public class ProcessThread implements Runnable {
    private BlockingQueue<Event> queue;
    private CollectContext collectContext;
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private final static String FACE = "face";
    private final static String PERSON = "person";
    private final static String CAR = "car";

    public ProcessThread(BlockingQueue<Event> queue, CollectContext collectContext) {
        this.queue = queue;
        this.collectContext = collectContext;
    }

    @Override
    public void run() {
        Event event;
        try {
            while ((event = queue.take()) != null) {
                byte[] bytes = FileUtil.fileToByteArray(event.getbAbsolutePath());
                Parser parser = event.getParser();
                //BufferedImage image = ImageIO.read(new ByteArrayInputStream(bytes));
                //取消分辨率判断
                //if (image.getWidth() * image.getHeight() < 1920 * 1080) {
                //log.error("Camera error, This is a small picture, fileName: " + event.getbAbsolutePath());
                //continue;
                //}
                if (collectContext.getFtpTypeList().contains("face")) {
                    ArrayList<SmallImage> smallImageList = FaceJNI.bigPictureCheck(bytes, PictureFormat.JPG);
                    if (smallImageList != null && smallImageList.size() > 0) {
                        int index = 1;
                        for (SmallImage smallImage : smallImageList) {
                            if (smallImage.getPictureStream() == null || smallImage.getPictureStream().length == 0) {
                                log.info("Face small image are not extracted, index: " + index + " fileName: " + event.getbAbsolutePath());
                                continue;
                            }
                            if (smallImage.getFaceAttribute() == null) {
                                log.info("Face attribute are not extracted, index: " + index + " fileName: " + event.getbAbsolutePath());
                                continue;
                            }
                            if (smallImage.getFaceAttribute().getFeature() == null
                                    || smallImage.getFaceAttribute().getFeature().length == 0) {
                                log.info("Face feature are not extracted, index: " + index + " fileName: " + event.getbAbsolutePath());
                                continue;
                            }
                            //保存图片
                            String smallImagePath = parser.path_b2s(event.getbAbsolutePath(), FACE, index);
                            boolean boo = ImageUtil.save(smallImagePath, smallImage.getPictureStream());
                            if (boo) {
                                String smallFtpUrlPath = parser.ftpUrl_b2s(event.getbFtpUrl(), FACE, index);
                                event.setsAbsolutePath(smallImagePath)
                                        .setsFtpUrl(smallFtpUrlPath)
                                        .setsRelativePath(parser.ftpUrl_b2s(event.getbRelativePath(), FACE, index));
                                this.sendKafka(event, smallImage.getFaceAttribute());
                                this.sendRocketMQ(event, collectContext.getRocketmqFaceTopic());
                            }
                            index++;
                        }
                    }
                } else {
                    log.warn("Face check failed, fileName:" + event.getbAbsolutePath());
                }

                List<Person> personList = null;
                List<Vehicle> vehicleList = null;
                if (collectContext.getFtpTypeList().contains("person") || collectContext.getFtpTypeList().contains("car")) {
                    ImageResult result = ImageToData.getImageResult(collectContext.getSeemmoUrl(), bytes, null);
                    if (result != null) {
                        personList = result.getPersonList();
                        vehicleList = result.getVehicleList();
                    }
                }
                if (collectContext.getFtpTypeList().contains("person") && personList != null && personList.size() > 0) {
                    int index = 1;
                    for (Person person : personList) {
                        if (person.getCar_data() == null || person.getCar_data().length == 0) {
                            log.info("Person small image are not extracted, fileName: " + event.getbAbsolutePath());
                            continue;
                        }
                        String smallImagePath = parser.path_b2s(event.getbAbsolutePath(), PERSON, index);
                        boolean boo = ImageUtil.save(smallImagePath, person.getCar_data());
                        if (boo) {
                            String smallFtpUrlPath = parser.ftpUrl_b2s(event.getbFtpUrl(), PERSON, index);
                            event.setsAbsolutePath(smallImagePath)
                                    .setsFtpUrl(smallFtpUrlPath)
                                    .setsRelativePath(parser.path_b2s(event.getbRelativePath(), PERSON, index));
                            this.sendKafka(event, person);
                            this.sendRocketMQ(event, collectContext.getRocketmqPersonTopic());
                        }
                        index++;
                    }
                }
                if (collectContext.getFtpTypeList().contains("car") && vehicleList != null && vehicleList.size() > 0) {
                    int index = 1;
                    for (Vehicle vehicle : vehicleList) {
                        if (vehicle.getVehicle_data() == null || vehicle.getVehicle_data().length == 0) {
                            log.info("Vehicle small image are not extracted, fileName: " + event.getbAbsolutePath());
                            continue;
                        }
                        String smallImagePath = parser.path_b2s(event.getbAbsolutePath(), CAR, index);
                        boolean boo = ImageUtil.save(smallImagePath, vehicle.getVehicle_data());
                        if (boo) {
                            String smallFtpUrlPath = parser.ftpUrl_b2s(event.getbFtpUrl(), CAR, index);
                            event.setbAbsolutePath(smallImagePath)
                                    .setsFtpUrl(smallFtpUrlPath)
                                    .setsRelativePath(parser.path_b2s(event.getbRelativePath(), CAR, index));
                            this.sendKafka(event, vehicle);
                            this.sendRocketMQ(event, collectContext.getRocketmqCarTopic());
                        }
                        index++;
                    }
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void sendKafka(Event event, FaceAttribute faceAttribute) {
        String faceId = UuidUtil.getUuid();
        FaceObject faceObject = FaceObject.builder()
                .setIpcId(event.getIpcId())
                .setTimeStamp(event.getTimeStamp())
                .setAttribute(faceAttribute)
                .setsFtpUrl(event.getsFtpUrl())
                .setbFtpUrl(event.getbFtpUrl())
                .setbAbsolutePath(event.getbAbsolutePath())
                .setsAbsolutePath(event.getsAbsolutePath())
                .setHostname(event.getHostname())
                .setId(faceId)
                .setIp(collectContext.getFtpIp())
                .setsRelativePath(event.getsRelativePath())
                .setbRelativePath(event.getbRelativePath());
        collectContext.getKafkaProducer().sendKafkaMessage(
                collectContext.getKafkaFaceObjectTopic(),
                faceId,
                JacksonUtil.toJson(faceObject),
                new KafkaCallBack(event.getsFtpUrl(), sdf.format(System.currentTimeMillis())));
    }

    private void sendKafka(Event event, Person person) {
        person.setCar_data(null);
        String pesonId = UuidUtil.getUuid();
        PersonObject personObject = PersonObject.builder()
                .setIpcId(event.getIpcId())
                .setTimeStamp(event.getTimeStamp())
                .setAttribute(person)
                .setHostname(event.getHostname())
                .setbAbsolutePath(event.getbAbsolutePath())
                .setsAbsolutePath(event.getsAbsolutePath())
                .setbFtpUrl(event.getbFtpUrl())
                .setsFtpUrl(event.getsFtpUrl())
                .setIp(collectContext.getFtpIp())
                .setsRelativePath(event.getsRelativePath())
                .setbRelativePath(event.getbRelativePath());

        collectContext.getKafkaProducer().sendKafkaMessage(
                collectContext.getKafkaPersonObjectTopic(),
                pesonId,
                JacksonUtil.toJson(personObject),
                new KafkaCallBack(event.getsFtpUrl(), sdf.format(System.currentTimeMillis())));
    }

    private void sendKafka(Event event, Vehicle vehicle) {
        vehicle.setVehicle_data(null);
        String carId = UuidUtil.getUuid();
        CarObject carObject = CarObject.builder()
                .setIpcId(event.getIpcId())
                .setTimeStamp(event.getTimeStamp())
                .setAttribute(vehicle)
                .setHostname(event.getHostname())
                .setbAbsolutePath(event.getbAbsolutePath())
                .setsAbsolutePath(event.getsAbsolutePath())
                .setbFtpUrl(event.getbFtpUrl())
                .setsFtpUrl(event.getsFtpUrl())
                .setIp(collectContext.getFtpIp())
                .setsRelativePath(event.getsRelativePath())
                .setbRelativePath(event.getbRelativePath());
        collectContext.getKafkaProducer().sendKafkaMessage(
                collectContext.getKafkaCarObjectTopic(),
                carId,
                JacksonUtil.toJson(carObject),
                new KafkaCallBack(event.getsFtpUrl(), sdf.format(System.currentTimeMillis())));
    }

    private void sendRocketMQ(Event event, String topic) {
        if (collectContext.getFtpSubscribeSwitch()) {
            // ftpSubscribeMap: key is ipcId, value is sessionIds
            Map<String, List<String>> ftpSubscribeMap = FtpSubscribeClient.getSessionMap();
            if (!ftpSubscribeMap.isEmpty()) {
                if (ftpSubscribeMap.containsKey(event.getIpcId())) {
                    List<String> sessionIds = ftpSubscribeMap.get(event.getIpcId());
                    SendMqMessage mqMessage = new SendMqMessage();
                    mqMessage.setSessionIds(sessionIds);
                    mqMessage.setFtpUrl(event.getsIpcFtpUrl());
                    collectContext.getRocketMQProducer().send(
                            topic,
                            event.getIpcId(),
                            event.getTimeStamp(),
                            JacksonUtil.toJson(mqMessage).getBytes());
                }
            }
        }
    }
}
