package com.hzgc.cluster.peoman.worker.service;

import com.hzgc.common.service.peoman.SyncPeopleManager;
import com.hzgc.common.util.json.JacksonUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import sun.misc.BASE64Decoder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

@Slf4j
@Component
public class InnerConsumer implements Runnable {
    @Autowired
    @SuppressWarnings("unused")
    private MemeoryCache memeoryCache;

    @Autowired
    @SuppressWarnings("unused")
    private Environment environment;

    @Value("kafka.bootstrap.servers")
    @SuppressWarnings("unused")
    private String kafkaHost;

    @Value("kafka.inner.group.id")
    @SuppressWarnings("unused")
    private String groupId;

    @Value("kafka.inner.topic")
    @SuppressWarnings("unused")
    private String innerTopic;

    @Value("kafka.inner.topic.polltime")
    @SuppressWarnings("unused")
    private Long pollTime;

    private KafkaConsumer<String, String> consumer;

    public void initInnerConsumer() {
        Properties properties = new Properties();
        properties.put("bootstrap.servers", kafkaHost);
        properties.put("group.id", groupId);
        properties.put("key.deserializer", StringDeserializer.class.getName());
        properties.put("value.deserializer", StringDeserializer.class.getName());
        consumer = new KafkaConsumer<>(properties);
        consumer.subscribe(Collections.singletonList(innerTopic));
    }

    @Override
    public void run() {
        while (true) {
            ConsumerRecords<String, String> records = consumer.poll(pollTime);
            List<SyncPeopleManager> managers = new ArrayList<>();
            for (ConsumerRecord<String, String> record : records) {
                SyncPeopleManager message = JacksonUtil.toObject(record.value(), SyncPeopleManager.class);
                String type = message.getType();
                switch (type) {
                    case "2":
                        managers.add(message);
                        break;
                }
            }
            addPerson(managers);
        }
    }

    /**
     * 添加人员
     * @param managerList 消息对象
     */
    private void addPerson(List<SyncPeopleManager> managerList) {
        List<ComparePicture> newComparePicture = new ArrayList<>();
        for (SyncPeopleManager message : managerList) {
            List<ComparePicture> comparePictureList = memeoryCache.getPeople(message.getPersonid());
            if (comparePictureList != null) {
                BASE64Decoder base64Decoder = new BASE64Decoder();
                ComparePicture comparePicture = new ComparePicture();
                comparePicture.setPeopleId(message.getPersonid());
                try {
                    comparePicture.setBitFeature(base64Decoder.decodeBuffer(message.getBitFeature()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                comparePicture.setId(message.getPictureId());
                newComparePicture.add(comparePicture);
            }
        }
        memeoryCache.putData(newComparePicture);
    }
}
