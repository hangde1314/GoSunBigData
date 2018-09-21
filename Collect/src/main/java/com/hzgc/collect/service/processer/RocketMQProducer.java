package com.hzgc.collect.service.processer;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.MessageQueueSelector;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageQueue;

import java.io.Serializable;
import java.util.List;

@Slf4j
public class RocketMQProducer implements Serializable {
    private DefaultMQProducer defaultMQProducer;

    public RocketMQProducer(String rokcetmqCaptureGroup, String rocketmqAddress) {
        defaultMQProducer = new DefaultMQProducer(rokcetmqCaptureGroup);
        defaultMQProducer.setRetryTimesWhenSendFailed(4);
        defaultMQProducer.setRetryAnotherBrokerWhenNotStoreOK(true);
        defaultMQProducer.setNamesrvAddr(rocketmqAddress);
        try {
            defaultMQProducer.start();
        } catch (MQClientException e) {
            log.error("DefaultMQProducer init error...");
            e.printStackTrace();
        }
    }

    public SendResult send(String topic, String tag, String key, byte[] data) {
        return send(topic, tag, key, data, null);
    }

    public SendResult send(String topic, String tag, String key, byte[] data, final MessageQueueSelector selector) {
        SendResult sendResult = null;
        try {
            Message msg;
            if (tag == null || tag.length() == 0) {
                msg = new Message(topic, data);
            } else if (key == null || key.length() == 0) {
                msg = new Message(topic, tag, data);
            } else {
                msg = new Message(topic, tag, key, data);
            }
            if (selector != null) {
                sendResult = defaultMQProducer.send(msg, new MessageQueueSelector() {
                    public MessageQueue select(List<MessageQueue> mqs, Message msg, Object arg) {
                        return selector.select(mqs, msg, arg);
                    }
                }, key);
            } else {
                sendResult = defaultMQProducer.send(msg);
            }
            log.info("Send RocketMQ successfully! message:[topic:" + msg.getTopic() + ", tag:" + msg.getTags() +
                    ", key:" + msg.getKeys() + ", data:" + new String(data) + "], " + sendResult);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Send message error...");
        }
        return sendResult;
    }
}
