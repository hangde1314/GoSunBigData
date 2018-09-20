package com.hzgc.collect.service.receiver;

import com.hzgc.collect.config.CollectConfiguration;
import org.apache.log4j.Logger;

import java.io.Serializable;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class ReceiverImpl implements Receiver, Serializable {

    private static Logger LOG = Logger.getLogger(ReceiverImpl.class);

    private BlockingQueue<Event> queue;
    private String queueID;

    ReceiverImpl(String queueID) {
        this.queueID = queueID;
        this.queue = new ArrayBlockingQueue<>(CollectConfiguration.getReceiveQueueCapacity());
    }

    @Override
    public void putData(Event event) {
        if (event != null) {
            try {
                queue.put(event);
                LOG.info("current queue is:" + queueID + ", the size  waiting is th queue is:" + getQueue().size());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void registerIntoContainer() {
    }

    @Override
    public void startProcess() {

    }

    @Override
    public BlockingQueue<Event> getQueue() {
        return this.queue;
    }
}
