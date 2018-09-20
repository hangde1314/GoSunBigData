package com.hzgc.cluster.peoman.worker.service;

import com.hzgc.common.service.peoman.ZookeeperProtocol;
import com.hzgc.common.util.zookeeper.Curator;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.CreateMode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class WorkerRegister {
    private Curator curator;

    @Value("lts.tasktracker.node-group")
    @SuppressWarnings("unused")
    private String workerId;

    @Value("zk.host")
    @SuppressWarnings("unused")
    private String zkAddress;

    public void init() {
        curator = new Curator(zkAddress);
    }

    public void regist() {
        curator.createNode(ZookeeperProtocol.rootPath + "/" + workerId,
                null, CreateMode.EPHEMERAL);
        log.info("Regist worker successfull ,worker id is ?", workerId);
    }
}
