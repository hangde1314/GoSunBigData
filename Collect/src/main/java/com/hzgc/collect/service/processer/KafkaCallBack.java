package com.hzgc.collect.service.processer;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.log4j.Logger;

import java.io.Serializable;

@Slf4j
public class KafkaCallBack implements Callback,Serializable {

    private String  elapsedTime;
    private String key;

    KafkaCallBack(String key, String time) {
        this.key = key;
        this.elapsedTime = time;
    }

    @Override
    public void onCompletion(RecordMetadata metadata, Exception e) {
        if (metadata != null) {
            log.info("Send Kafka successfully! message:[topic:" + metadata.topic() + ", key:" + key +
                    "], send to partition(" + metadata.partition() + "), offset(" + metadata.offset() + ") in " + elapsedTime + "ms");
        } else {
            log.error(e.getMessage());
        }
    }
}
