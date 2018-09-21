package com.hzgc.collect.service.processer;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.io.Serializable;
import java.util.Properties;

@Slf4j
public class KafkaProducer implements Serializable {
    private org.apache.kafka.clients.producer.KafkaProducer<String, String> kafkaProducer;

    public KafkaProducer(Properties properties) {
        kafkaProducer = new org.apache.kafka.clients.producer.KafkaProducer<>(properties);
        log.info("Create KafkaProducer successfully!");
    }

    void sendKafkaMessage(final String topic, final String key, final String value, final Callback callBack) {
        kafkaProducer.send(new ProducerRecord<>(topic, key, value), callBack);
    }
}

