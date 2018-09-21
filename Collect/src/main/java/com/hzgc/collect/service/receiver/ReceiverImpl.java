package com.hzgc.collect.service.receiver;

import com.hzgc.collect.config.CollectContext;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

@Slf4j
public class ReceiverImpl implements Receiver, Serializable {

    private BlockingQueue<Event> queue;
    private String queueID;

    ReceiverImpl(String queueID, CollectContext collectContext) {
        this.queueID = queueID;
        this.queue = new ArrayBlockingQueue<>(collectContext.getReceiveQueueCapacity());
    }

    @Override
    public void putData(Event event) {
        if (event != null) {
            try {
                queue.put(event);
                log.info("current queue is:" + queueID + ", the size  waiting is th queue is:" + getQueue().size());
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
