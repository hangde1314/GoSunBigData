package com.hzgc.collect.service.processer;

import com.hzgc.collect.config.CollectConfiguration;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.log4j.Logger;

import java.io.Serializable;
import java.util.Properties;

public class KafkaProducer implements Serializable {

    private static Logger LOG = Logger.getLogger(KafkaProducer.class);
    public static KafkaProducer instance;
    private static org.apache.kafka.clients.producer.KafkaProducer<String, String> kafkaProducer;

    private KafkaProducer() {
        Properties kafkProper = new Properties();
        CollectConfiguration.getProps().forEach((key, value) -> {
            if (((String)key).contains("kafka.") && !((String)key).contains("topic")) {
                kafkProper.setProperty(((String) key).replace("kafka.", ""),
                        (String) value);
            }
        });
        kafkaProducer = new org.apache.kafka.clients.producer.KafkaProducer<>(kafkProper);
        LOG.info("Create KafkaProducer successfully!");
    }

    void sendKafkaMessage(final String topic, final String key, final String value, final Callback callBack) {
        kafkaProducer.send(new ProducerRecord<>(topic, key, value), callBack);
    }

    public static KafkaProducer getInstance() {
        if (instance == null) {
            synchronized (KafkaProducer.class) {
                if (instance == null){
                    instance = new KafkaProducer();
                }
            }
        }
        return instance;
    }
}

