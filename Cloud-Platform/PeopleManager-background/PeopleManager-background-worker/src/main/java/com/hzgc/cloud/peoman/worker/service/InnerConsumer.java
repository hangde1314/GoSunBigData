package com.hzgc.cloud.peoman.worker.service;

import com.hzgc.cloud.peoman.worker.dao.PictureMapper;
import com.hzgc.cloud.peoman.worker.model.Picture;
import com.hzgc.common.service.peoman.SyncPeopleManager;
import com.hzgc.common.util.json.JacksonUtil;
import com.hzgc.jniface.FaceUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

@Slf4j
@Component
public class InnerConsumer implements Runnable {
    @Autowired
    @SuppressWarnings("unused")
    private PictureMapper pictureMapper;

    @Autowired
    @SuppressWarnings("unused")
    private MemeoryCache memeoryCache;

    @Autowired
    @SuppressWarnings("unused")
    private Environment environment;

    @Value("${kafka.bootstrap.servers}")
    @SuppressWarnings("unused")
    private String kafkaHost;

    @Value("${kafka.inner.group.id}")
    @SuppressWarnings("unused")
    private String groupId;

    @Value("${kafka.inner.topic}")
    @SuppressWarnings("unused")
    private String innerTopic;

    @Value("${kafka.inner.topic.polltime}")
    @SuppressWarnings("unused")
    private Long pollTime;

    private KafkaConsumer<String, String> consumer;

    public void initInnerConsumer(String workId) {
        Properties properties = new Properties();
        properties.put("bootstrap.servers", kafkaHost);
//        properties.put("group.id", workId);
        properties.put("group.id", workId);
        properties.put("key.deserializer", StringDeserializer.class.getName());
        properties.put("value.deserializer", StringDeserializer.class.getName());
        consumer = new KafkaConsumer<>(properties);
        consumer.subscribe(Collections.singletonList(innerTopic));
    }

    /**
     * type 类型（2：添加人员， 3：修改人员， 4：删除人员）
     */
    @Override
    public void run() {
        while (true) {
            ConsumerRecords<String, String> records = consumer.poll(pollTime);
            List<SyncPeopleManager> managers = new ArrayList<>();
            for (ConsumerRecord<String, String> record : records) {
                SyncPeopleManager message = JacksonUtil.toObject(record.value(), SyncPeopleManager.class);
                log.info("PeoMan-Inner message : " + JacksonUtil.toJson(message));
                String type = message.getType();
                switch (type) {
                    case "2":
                        managers.add(message);
                        break;
                    case "3":
                        managers.add(message);
                        break;
                    case "4":
                        managers.add(message);
                        break;
                }
            }
            addPerson(managers);
        }
    }

    /**
     * 添加人员
     *
     * @param managerList 消息对象
     */
    private void addPerson(List<SyncPeopleManager> managerList) {
        if (managerList != null && managerList.size() > 0) {
            memeoryCache.delData(managerList);
            List<ComparePicture> newComparePicture = new ArrayList<>();
            for (SyncPeopleManager message : managerList) {
                List<Picture> pictureList = pictureMapper.selectByPeopleId(message.getPersonid());
                if (pictureList != null) {
                    for (Picture picture : pictureList) {
                        ComparePicture comparePicture = new ComparePicture();
                        comparePicture.setId(picture.getId());
                        comparePicture.setPeopleId(picture.getPeopleId());
                        comparePicture.setBitFeature(FaceUtil.base64Str2BitFeature(picture.getBitFeature()));
                        comparePicture.setFlagId(picture.getFlagId());
                        comparePicture.setName(picture.getName());
                        newComparePicture.add(comparePicture);
                    }
                }
            }

            memeoryCache.putData(newComparePicture);
        }
    }

}
