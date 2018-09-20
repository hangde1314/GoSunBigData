package com.hzgc.collect.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;

@Component
@Data
public class CollectConfiguration implements Serializable {
    @Autowired
    private Environment environment;

    @Value("${receive.queue.capacity}")
    @NotNull
    private Integer receiveQueueCapacity;

    @Value("${receive.number}")
    @NotNull
    private Integer receiveNumber;

    @Value("${face.detector.number}")
    @NotNull
    private Integer faceDetectorNumber;

    @Value("${ftp.type}")
    @NotNull
    private String ftpType;

    @Value("${ftp.ip}")
    @NotNull
    private String ftpIp;

    @Value("${ftp.port}")
    @NotNull
    private Integer ftpPort;

    @Value("${zookeeper.address}")
    @NotNull
    private String zookeeperAddress;

    @Value("${ftp.subscribe.switch}")
    @NotNull
    private Boolean ftpSubscribeSwitch;

    @Value("${kafka.faceobject.topic}")
    @NotNull
    private String kafkaFaceObjectTopic;

    @Value("${kafka.personobject.topic}")
    @NotNull
    private String kafkaPersonObjectTopic;

    @Value("${kafka.carobject.topic}")
    @NotNull
    private String kafkaCarObjectTopic;

    @Value("${rocketmq.address}")
    @NotNull
    private String rocketmqAddress;

    @Value("${rocketmq.face.topic}")
    @NotNull
    private String rocketmqFaceTopic;

    @Value("${rocketmq.person.topic}")
    @NotNull
    private String rocketmqPersonTopic;

    @Value("${rocketmq.car.topic}")
    @NotNull
    private String rocketmqCarTopic;

    @Value("${rocketmq.capture.group}")
    @NotNull
    private String rokcetmqCaptureGroup;

    @NotNull
    private String hostname = InetAddress.getLocalHost().getHostName();

    @Value("${ftp.version}")
    @NotNull
    private String ftpVersion;

    @Value("${zookeeper.register.path}")
    @NotNull
    private String registerPath;

    @Value("${ftp.pathRule}")
    @NotNull
    private String ftpPathRule;

    @Value("${ftp.account}")
    @NotNull
    private String ftpAccount;

    @Value("${ftp.password}")
    @NotNull
    private String ftpPassword;

    @Value("${homeDirs}")
    @NotNull
    private String homeDirs;

    @Value("${seemmo.url}")
    @NotNull
    private String seemmoUrl;

    @Value("${diskUsageRate}")
    @NotNull
    private float diskUsageRate;

    @Value("${period}")
    @NotNull
    private long period;

    public CollectConfiguration() throws UnknownHostException {
    }
}
