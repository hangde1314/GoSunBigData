package com.hzgc.cloud.peoman.worker.service;

import com.hzgc.common.collect.bean.FaceObject;
import com.hzgc.common.util.json.JacksonUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Properties;

@Slf4j
@Component
public class FaceConsumer implements Runnable{
    @Autowired
    @SuppressWarnings("unused")
    private PeopleCompare peopleCompare;

    @Value("${kafka.bootstrap.servers}")
    @SuppressWarnings("unused")
    private String kafkaHost;

    @Value("${kafka.face.topic}")
    @SuppressWarnings("unused")
    private String faceTopic;

    @Value("${kafka.face.groupId}")
    @SuppressWarnings("unused")
    private String faceGroupId;

    @Value("${kafka.face.topic.polltime}")
    @SuppressWarnings("unused")
    private Long pollTime;

    @Value("${face.sharpness.open}")
    @SuppressWarnings("unused")
    private boolean isSharpnessOpen;

    private KafkaConsumer<String, String> consumer;

    public void initFaceConsumer() {
        Properties properties = new Properties();
        properties.put("group.id", faceGroupId);
        properties.put("bootstrap.servers", kafkaHost);
        properties.put("key.deserializer", StringDeserializer.class.getName());
        properties.put("value.deserializer", StringDeserializer.class.getName());
        properties.put("max.poll.records","50");
        consumer = new KafkaConsumer<>(properties);
        consumer.subscribe(Collections.singletonList(faceTopic));
        log.info("topic="+faceTopic+", groupid="+faceGroupId+",kafkaHost="+kafkaHost);
    }

    @Override
    public void run() {
        while (true) {
            ConsumerRecords<String, String> records = consumer.poll(pollTime);
            for (ConsumerRecord<String, String> record : records) {
//                log.info("====================kafka value="+record.value());
                if (record.value() != null && record.value().length() > 0) {
                    log.info("===============================PeopleCompare Start===============================");
                    FaceObject faceObject = JacksonUtil.toObject(record.value(), FaceObject.class);
                    if (isSharpnessOpen) {
                        if (faceObject.getAttribute().getSharpness() == 1) {
                            peopleCompare.comparePeople(faceObject);
                        } else {
                            log.info("FaceObject data is invalid　exit!, sharpness is {}, sFtpUrl={}", faceObject.getAttribute().getSharpness(), faceObject.getsFtpUrl());
                        }
                    } else {
                        peopleCompare.comparePeople(faceObject);
                    }


                    log.info("===============================PeopleCompare End=================================");
                }
            }
        }
    }

}