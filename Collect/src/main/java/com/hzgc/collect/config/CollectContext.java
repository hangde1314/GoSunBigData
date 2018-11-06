package com.hzgc.collect.config;

import com.hzgc.collect.service.ftp.ftplet.FtpHomeDir;
import com.hzgc.collect.service.ftp.util.BaseProperties;
import com.hzgc.collect.service.parser.FtpPathBootStrap;
import com.hzgc.common.collect.facedis.FtpRegisterClient;
import com.hzgc.common.collect.facedis.FtpRegisterInfo;
import com.hzgc.common.collect.facesub.FtpSubscribeClient;
import com.hzgc.jniface.FaceFunction;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;

@Component
@Data
@Slf4j
public class CollectContext implements Serializable {
    @Autowired
    private Environment environment;
    @Autowired
    private PlateCheck plateCheck;

    @Value("${receive.queue.capacity}")
    @NotNull
    private Integer receiveQueueCapacity;

    @Value("${receive.number}")
    @NotNull
    private Integer receiveNumber;

    @Value("${face.detector.number}")
    @NotNull
    private Integer faceDetectorNumber;

    @Value("${ftp.ip}")
    @NotNull
    private String ftpIp;

    @Value("${ftp.port}")
    @NotNull
    private Integer ftpPort;

    @Value("${zookeeper.address}")
    @NotNull
    private String zookeeperAddress;

    @Value("${kafka.faceobject.topic}")
    @NotNull
    private String kafkaFaceObjectTopic;

    @Value("${kafka.personobject.topic}")
    @NotNull
    private String kafkaPersonObjectTopic;

    @Value("${kafka.carobject.topic}")
    @NotNull
    private String kafkaCarObjectTopic;

    @NotNull
    private String hostname = InetAddress.getLocalHost().getHostName();

    @Value("${ftp.version}")
    @NotNull
    private String ftpVersion;

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

    @Value("${face.detector.enable}")
    private Boolean faceDetectorOpen;

    // 初始化 ftp 当前已满磁盘、未满磁盘、RootDir
    @Autowired
    private FtpHomeDir ftpHomeDir;

    @Autowired
    //Spring-kafka-templage
    private KafkaTemplate<String, String> kafkaTemplate;

    private FtpRegisterClient ftpRegisterClient;

    private FtpSubscribeClient ftpSubscribeClient;

    private FtpPathBootStrap ftpPathBootStrap;


    public CollectContext() throws UnknownHostException {
    }

    /*
     * 加载有顺序
     */
    public void initAll() {
        if (faceDetectorOpen) {
            initDetector();
        }
        initFtpPathBoostrap();
        initFtpHomeDirCheck();
        initFtpRegisterClient();
        initFtpSubscribeClient();
    }

    private void initFtpPathBoostrap() {
        ftpPathBootStrap = new FtpPathBootStrap(this);
    }

    private void initDetector() {
        log.info("Init face detector, number is " + faceDetectorNumber);
        if (faceDetectorNumber == 0) {
            FaceFunction.init();
        } else {
            for (int i = 0; i < faceDetectorNumber; i++) {
                FaceFunction.init();
            }
        }
    }

    private void initFtpHomeDirCheck() {
        ftpHomeDir.periodicallyCheckCurrentRootDir();
    }

    private void initFtpSubscribeClient() {
        ftpSubscribeClient = new FtpSubscribeClient(zookeeperAddress);
    }

    private void initFtpRegisterClient() {
        ftpRegisterClient = new FtpRegisterClient(zookeeperAddress);
        ftpRegisterClient.createNode(new FtpRegisterInfo(null, null, ftpPathRule,
                ftpAccount, ftpPassword, ftpIp, hostname, ftpPort + "", "face,car,person"));
    }

    public BaseProperties getUserMangerProperties() {
        BaseProperties properties = new BaseProperties();
        properties.setProperty("com.hzgc.ftpserver.user.admin.userpassword",
                environment.getProperty("com.hzgc.ftpserver.user.admin.userpassword"));
        properties.setProperty("com.hzgc.ftpserver.user.admin.homedirectory",
                environment.getProperty("com.hzgc.ftpserver.user.admin.homedirectory"));
        properties.setProperty("com.hzgc.ftpserver.user.admin.enableflag",
                environment.getProperty("com.hzgc.ftpserver.user.admin.enableflag"));
        properties.setProperty("com.hzgc.ftpserver.user.admin.writepermission",
                environment.getProperty("com.hzgc.ftpserver.user.admin.writepermission"));
        properties.setProperty("com.hzgc.ftpserver.user.admin.maxloginnumber",
                environment.getProperty("com.hzgc.ftpserver.user.admin.maxloginnumber"));
        properties.setProperty("com.hzgc.ftpserver.user.admin.maxloginperip",
                environment.getProperty("com.hzgc.ftpserver.user.admin.maxloginperip"));
        properties.setProperty("com.hzgc.ftpserver.user.admin.idletime",
                environment.getProperty("com.hzgc.ftpserver.user.admin.idletime"));
        properties.setProperty("com.hzgc.ftpserver.user.admin.uploadrate",
                environment.getProperty("com.hzgc.ftpserver.user.admin.uploadrate"));
        properties.setProperty("com.hzgc.ftpserver.user.admin.downloadrate",
                environment.getProperty("com.hzgc.ftpserver.user.admin.downloadrate"));
        properties.setProperty("com.hzgc.ftpserver.user.admin.groups",
                environment.getProperty("com.hzgc.ftpserver.user.admin.groups"));
        properties.setProperty("com.hzgc.ftpserver.user.anonymous.userpassword",
                environment.getProperty("com.hzgc.ftpserver.user.anonymous.userpassword"));
        properties.setProperty("com.hzgc.ftpserver.user.anonymous.homedirectory",
                environment.getProperty("com.hzgc.ftpserver.user.anonymous.homedirectory"));
        properties.setProperty("com.hzgc.ftpserver.user.anonymous.enableflag",
                environment.getProperty("com.hzgc.ftpserver.user.anonymous.enableflag"));
        properties.setProperty("com.hzgc.ftpserver.user.anonymous.writepermission",
                environment.getProperty("com.hzgc.ftpserver.user.anonymous.writepermission"));
        properties.setProperty("com.hzgc.ftpserver.user.anonymous.maxloginnumber",
                environment.getProperty("com.hzgc.ftpserver.user.anonymous.maxloginnumber"));
        properties.setProperty("com.hzgc.ftpserver.user.anonymous.maxloginperip",
                environment.getProperty("com.hzgc.ftpserver.user.anonymous.maxloginperip"));
        properties.setProperty("com.hzgc.ftpserver.user.anonymous.idletime",
                environment.getProperty("com.hzgc.ftpserver.user.anonymous.idletime"));
        properties.setProperty("com.hzgc.ftpserver.user.anonymous.uploadrate",
                environment.getProperty("com.hzgc.ftpserver.user.anonymous.uploadrate"));
        properties.setProperty("com.hzgc.ftpserver.user.anonymous.downloadrate",
                environment.getProperty("com.hzgc.ftpserver.user.anonymous.downloadrate"));
        properties.setProperty("com.hzgc.ftpserver.user.anonymous.groups",
                environment.getProperty("com.hzgc.ftpserver.user.anonymous.groups"));
        properties.setProperty("com.hzgc.ftpserver.user.maxLoginFailures",
                environment.getProperty("com.hzgc.ftpserver.user.maxLoginFailures"));
        properties.setProperty("com.hzgc.ftpserver.user.loginFailureDelay",
                environment.getProperty("com.hzgc.ftpserver.user.loginFailureDelay"));
        properties.setProperty("com.hzgc.ftpserver.user.maxThreads",
                environment.getProperty("com.hzgc.ftpserver.user.maxThreads"));
        return properties;
    }

    public int getConnectionMaxLogins() {
        return Integer.parseInt(environment.getProperty("com.hzgc.ftpserver.user.admin.maxloginnumber"));
    }

    public boolean getConnectionAnonymousLoginEnable() {
        return Boolean.parseBoolean(environment.getProperty("com.hzgc.ftpserver.user.anonymous.enableflag"));
    }

    public int getConnectionMaxAnonymousLogins() {
        return Integer.parseInt(environment.getProperty("com.hzgc.ftpserver.user.anonymous.maxloginnumber"));
    }

    public int getConnectionMaxLoginFailures() {
        return Integer.parseInt(environment.getProperty("com.hzgc.ftpserver.user.maxLoginFailures"));
    }

    public int getConnectionLoginFailureDelay() {
        return Integer.parseInt(environment.getProperty("com.hzgc.ftpserver.user.loginFailureDelay"));
    }

    public int getConnectionMaxThread() {
        return Integer.parseInt(environment.getProperty("com.hzgc.ftpserver.user.maxThreads"));
    }

    public String getLogo() {
        return " _____  _             ____                                 \n" +
                "|  ___|| |_  _ __    / ___|   ___  _ __ __   __  ___  _ __ \n" +
                "| |_   | __|| '_ \\   \\___ \\  / _ \\| '__|\\ \\ / / / _ \\| '__|\n" +
                "|  _|  | |_ | |_) |   ___) ||  __/| |    \\ V / |  __/| |   \n" +
                "|_|     \\__|| .__/   |____/  \\___||_|     \\_/   \\___||_|       version " + ftpVersion + "\n" +
                "            |_|";
    }
}
