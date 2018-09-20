package com.hzgc.collect.service.receiver;

import java.util.concurrent.BlockingQueue;

public interface Receiver {

    /**
     * 此方法可将数据插入当前Receiver的队列列
     *
     * @param data 数据对象
     */
    public void putData(Event data);

    /**
     * 向RecvicerContainer注册Recvicer用来接收数据
     */
    public void registerIntoContainer();

    /**
     * 开始处理数据
     */
    public void startProcess();

    /**
     * 获取当前队列
     */
    public BlockingQueue<Event> getQueue();
}
