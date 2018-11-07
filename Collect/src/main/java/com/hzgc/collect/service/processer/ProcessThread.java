package com.hzgc.collect.service.processer;

import com.hzgc.collect.config.CollectContext;
import com.hzgc.collect.service.parser.Parser;
import com.hzgc.collect.service.receiver.Event;
import com.hzgc.common.collect.bean.CarObject;
import com.hzgc.common.collect.bean.FaceObject;
import com.hzgc.common.collect.bean.PersonObject;
import com.hzgc.common.util.basic.FileUtil;
import com.hzgc.common.util.basic.ImageUtil;
import com.hzgc.common.util.basic.UuidUtil;
import com.hzgc.common.util.json.JacksonUtil;
import com.hzgc.jniface.FaceAttribute;
import com.hzgc.jniface.FaceFunction;
import com.hzgc.jniface.PictureFormat;
import com.hzgc.jniface.SmallImage;
import com.hzgc.seemmo.bean.ImageResult;
import com.hzgc.seemmo.bean.carbean.Vehicle;
import com.hzgc.seemmo.bean.personbean.Person;
import com.hzgc.seemmo.service.ImageToData;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.concurrent.ListenableFuture;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;

@Slf4j
public class ProcessThread implements Runnable {
    private BlockingQueue <Event> queue;
    private CollectContext collectContext;
    private final static String FACE = "face";
    private final static String PERSON = "person";
    private final static String CAR = "car";
    private final static Integer WIDTH = 500;
    private final static Integer HEIGHT = 500;

    public ProcessThread(BlockingQueue <Event> queue, CollectContext collectContext) {
        this.queue = queue;
        this.collectContext = collectContext;
    }

    @Override
    public void run() {
        Event event;
        try {
            while ((event = queue.take()) != null) {
                byte[] bytes = FileUtil.fileToByteArray(event.getbAbsolutePath());
                if (bytes == null) {
                    continue;
                }
                try {
                    ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
                    BufferedImage read = ImageIO.read(inputStream);
                    int width = read.getWidth();
                    int height = read.getHeight();
                    if (!(width >= WIDTH && height >= HEIGHT)) {
                        continue;
                    }
                } catch (IOException e) {
                    log.error("Image size is not more than 500 * 500");
                }
                Parser parser = event.getParser();
                //BufferedImage image = ImageIO.read(new ByteArrayInputStream(bytes));
                //取消分辨率判断
                //if (image.getWidth() * image.getHeight() < 1920 * 1080) {
                //log.error("Camera error, This is a small picture, fileName: " + event.getbAbsolutePath());
                //continue;
                //}
                ArrayList <SmallImage> smallImageList = FaceFunction.faceCheck(bytes, PictureFormat.JPG, PictureFormat.LEVEL_WIDTH_1);
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
                        }
                        index++;
                    }
                } else {
                    log.warn("Face check failed, fileName:" + event.getbAbsolutePath());
                }

                List <Person> personList = null;
                List <Vehicle> vehicleList = null;
                ImageResult result = ImageToData.getImageResult(collectContext.getSeemmoUrl(), bytes, null);
                if (result != null) {
                    personList = result.getPersonList();
                    vehicleList = result.getVehicleList();
                } else {
                    log.error("Person or Car check failed, file name is:{}", event.getbAbsolutePath());
                }
                if (personList != null && personList.size() > 0) {
                    log.info("Person check successfull ,file name is:{}", event.getbAbsolutePath());
                    int index = 1;
                    for (Person person : personList) {
                        if (person.getCar_data() == null || person.getCar_data().length == 0
                                //临时添加行人检测人脸,用来提高检测质量
                                || !tem_person_check(person.getCar_data())) {
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
                        }
                        index++;
                    }
                } else {
                    log.warn("Person check failed, file name is:{}", event.getbAbsolutePath());
                }

                if (vehicleList != null && vehicleList.size() > 0) {
                    log.info("Car check successfull ,file name is:{}", event.getbAbsolutePath());
                    int index = 1;
                    for (Vehicle vehicle : vehicleList) {
                        if (null == vehicle.getVehicle_type()) {
                            log.info("Vehicle type is null, fileName: " + event.getbAbsolutePath());
                            continue;
                        }
                        if (vehicle.getVehicle_data() == null || vehicle.getVehicle_data().length == 0) {
                            log.info("Vehicle small image are not extracted, fileName: " + event.getbAbsolutePath());
                            continue;
                        }
                        //临时添加过滤,用来提高图片质量,过滤掉没有车牌的图片
                        if (vehicle.getPlate_licence() == null || vehicle.getPlate_licence().length() == 0) {
                            log.info("Vehicle small image is not plate_licence, fileName: " + event.getbAbsolutePath());
                            continue;
                        }
                        String smallImagePath = parser.path_b2s(event.getbAbsolutePath(), CAR, index);
                        boolean boo = ImageUtil.save(smallImagePath, vehicle.getVehicle_data());
                        if (boo) {
                            String smallFtpUrlPath = parser.ftpUrl_b2s(event.getbFtpUrl(), CAR, index);
                            event.setsAbsolutePath(smallImagePath)
                                    .setsFtpUrl(smallFtpUrlPath)
                                    .setsRelativePath(parser.path_b2s(event.getbRelativePath(), CAR, index));
                            this.sendKafka(event, vehicle);
                        }
                        index++;
                    }
                } else {
                    log.warn("Car check failed, file name is:{}", event.getbAbsolutePath());
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    //临时方法
    private boolean tem_person_check(byte[] personStream) {
        ArrayList <SmallImage> result =
                FaceFunction.faceCheck(personStream, PictureFormat.JPG, PictureFormat.LEVEL_WIDTH_3);
        return result != null && result.size() > 0;
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
                .setsRelativePath(event.getsRelativePath())
                .setbRelativePath(event.getbRelativePath())
                .setId(faceId)
                .setIp(collectContext.getFtpIp());
        ListenableFuture <SendResult <String, String>> resultFuture =
                collectContext.getKafkaTemplate().send(collectContext.getKafkaFaceObjectTopic(),
                        faceId,
                        JacksonUtil.toJson(faceObject));
        try {
            RecordMetadata metaData = resultFuture.get().getRecordMetadata();
            if (metaData != null) {
                log.info("Send Kafka successfully! message:[topic:{}, sAbsolutePath:{}, bAbsolutePath:{}]",
                        metaData.topic(), event.getsAbsolutePath(), event.getbAbsolutePath());
            }
        } catch (InterruptedException | ExecutionException e) {
            log.error(e.getMessage());
        }
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
                .setsRelativePath(event.getsRelativePath())
                .setbRelativePath(event.getbRelativePath())
                .setId(pesonId)
                .setIp(collectContext.getFtpIp());

        try {
            ListenableFuture <SendResult <String, String>> resultFuture =
                    collectContext.getKafkaTemplate().send(collectContext.getKafkaPersonObjectTopic(),
                            pesonId,
                            JacksonUtil.toJson(personObject));
            RecordMetadata metaData = resultFuture.get().getRecordMetadata();
            if (metaData != null) {
                log.info("Send Kafka successfully! message:[topic:{}, sAbsolutePath:{}, bAbsolutePath:{}]",
                        metaData.topic(), event.getsAbsolutePath(), event.getbAbsolutePath());
            } else {
                log.error("Send kafka failed! metaData is null");
            }
        } catch (InterruptedException | ExecutionException e) {
            log.error(e.getMessage());
        }
    }

    private void sendKafka(Event event, Vehicle vehicle) {
        if (collectContext.getPlateCheck().plateCheck(event.getIpcId(), vehicle.getPlate_licence())) {
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
                    .setsRelativePath(event.getsRelativePath())
                    .setbRelativePath(event.getbRelativePath())
                    .setId(carId)
                    .setIp(collectContext.getFtpIp());
            try {
                ListenableFuture <SendResult <String, String>> resultFuture =
                        collectContext.getKafkaTemplate().send(collectContext.getKafkaCarObjectTopic(),
                                carId,
                                JacksonUtil.toJson(carObject));
                RecordMetadata metaData = resultFuture.get().getRecordMetadata();
                if (metaData != null) {
                    log.info("Send Kafka successfully! message:[topic:{}, sAbsolutePath:{}, bAbsolutePath:{}]",
                            metaData.topic(), event.getsAbsolutePath(), event.getbAbsolutePath());
                }
            } catch (InterruptedException | ExecutionException e) {
                log.error(e.getMessage());
            }
        }
    }
}
