package com.hzgc.collect.service.ftp.impl;

import com.hzgc.collect.config.CollectConfiguration;
import com.hzgc.collect.service.receiver.ReceiverScheduler;

public class ReceiverFtpServerContext extends DefaultFtpServerContext {
    private ReceiverScheduler scheduler;

    public ReceiverFtpServerContext(CollectConfiguration configuration) {
        super();
        this.scheduler = new ReceiverScheduler(configuration);
    }

    @Override
    public ReceiverScheduler getScheduler() {
        return this.scheduler;
    }
}
