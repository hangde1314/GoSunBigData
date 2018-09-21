package com.hzgc.collect.service.receiver;

import com.hzgc.collect.service.processer.ProcessThread;
import com.hzgc.collect.config.CollectContext;
import com.hzgc.common.util.json.JacksonUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public class ReceiverScheduler implements Serializable {

    private final List<ReceiverImpl> container = new ArrayList<>();

    private CollectContext collectContext;

    /**
     * 配置文件中配置的receiver的数量
     */
    private int receiveNumber;

    /**
     * 用来计算调用getReceiver的次数，并用此数量和receiver的数量的余数，对receiver依次get
     */
    private int pollingCount;

    public ReceiverScheduler(CollectContext collectContext) {
        this.receiveNumber = collectContext.getReceiveNumber();
        this.collectContext = collectContext;
    }

    /**
     * 将封装的LogEvent的日志对象调用receiver的putData方法，写入
     * receiver队列中
     *
     * @param event 封装的数据对象
     */
    public void putData(final Event event) {
        synchronized (container) {
            log.debug("Thread name [" + Thread.currentThread() + "] process event " + JacksonUtil.toJson(event));
            getReceiver().putData(event);
        }
    }

    /**
     * 采用依次取用receiver的方法，根据每次调用getReceiver方法
     * 对pollingCount进行自增，根据pollingCount和receiverNumber
     * 的余值判断使用哪个receiver
     *
     * @return 返回Recvicer对象
     */
    private Receiver getReceiver() {
        Receiver receiver = container.get(this.pollingCount % this.receiveNumber);
        pollingCount++;
        return receiver;
    }

    /**
     * 将receiver注册至container容器中
     *
     * @param receiver 参数receiver，注册至容器中
     */
    private void register(ReceiverImpl receiver) {
        container.add(receiver);
    }

    /**
     * 根据配置文件中配置的receiverNumber和日志文件地址调用rebanceReceiver方法
     * 取得对应的queueIdList，根据这些queueId去初始化receiver
     */
    public void prepareReceiver() {
        log.info("Initialization receiver, receiver number is " + this.receiveNumber);
        if (this.receiveNumber != 0) {
            for (int i = 0; i < this.receiveNumber; i++) {
                //用来存放工作线程的线程池
                ExecutorService pool = Executors.newFixedThreadPool(receiveNumber);
                ReceiverImpl receiver = new ReceiverImpl(i + "", collectContext);
                register(receiver);
                pool.execute(new ProcessThread(receiver.getQueue(), collectContext));
            }
        }

    }
}
