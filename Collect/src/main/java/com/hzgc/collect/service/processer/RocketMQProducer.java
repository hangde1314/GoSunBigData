package com.hzgc.collect.service.processer;

import com.hzgc.collect.config.CollectConfiguration;
import org.apache.log4j.Logger;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.MessageQueueSelector;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageQueue;

import java.io.Serializable;
import java.util.List;

public class RocketMQProducer implements Serializable {
    private static Logger LOG = Logger.getLogger(RocketMQProducer.class);
    private static RocketMQProducer instance = null;
    private DefaultMQProducer defaultMQProducer;

    private RocketMQProducer(){
        defaultMQProducer = new DefaultMQProducer(CollectConfiguration.getRokcetmqCaptureGroup());
        defaultMQProducer.setRetryTimesWhenSendFailed(4);
        defaultMQProducer.setRetryAnotherBrokerWhenNotStoreOK(true);
        defaultMQProducer.setNamesrvAddr(CollectConfiguration.getRocketmqAddress());
        try {
            defaultMQProducer.start();
        } catch (MQClientException e) {
            LOG.error("DefaultMQProducer init error...");
            e.printStackTrace();
        }
    }

    public static RocketMQProducer getInstance() {
        if (instance == null) {
            synchronized (RocketMQProducer.class) {
                if (instance == null) {
                    instance = new RocketMQProducer();
                }
            }
        }
        return instance;
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
            LOG.info("Send RocketMQ successfully! message:[topic:" + msg.getTopic() + ", tag:" + msg.getTags() +
                    ", key:" + msg.getKeys() + ", data:" + new String(data) + "], " + sendResult);
        } catch (Exception e) {
            e.printStackTrace();
            LOG.error("Send message error...");
        }
        return sendResult;
    }
}
