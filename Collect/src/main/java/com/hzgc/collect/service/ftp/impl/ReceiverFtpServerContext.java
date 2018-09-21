package com.hzgc.collect.service.ftp.impl;

import com.hzgc.collect.config.CollectContext;
import com.hzgc.collect.service.receiver.ReceiverScheduler;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
public class ReceiverFtpServerContext extends DefaultFtpServerContext {
    private ReceiverScheduler scheduler;
    private CollectContext collectContext;

    public ReceiverFtpServerContext(CollectContext collectContext) {
        super();
        this.collectContext = collectContext;
        this.scheduler = new ReceiverScheduler(collectContext);
    }

    @Override
    public ReceiverScheduler getScheduler() {
        return this.scheduler;
    }

    @Override
    public CollectContext getCollectContext() {
        return this.collectContext;
    }
}
