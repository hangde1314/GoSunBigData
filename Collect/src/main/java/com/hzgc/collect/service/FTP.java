package com.hzgc.collect.service;

import com.hzgc.collect.config.CollectContext;
import com.hzgc.collect.service.ftp.ClusterOverFtp;
import com.hzgc.collect.service.ftp.ConnectionConfigFactory;
import com.hzgc.collect.service.ftp.FtpServer;
import com.hzgc.collect.service.ftp.FtpServerFactory;
import com.hzgc.collect.service.ftp.command.CommandFactoryFactory;
import com.hzgc.collect.service.ftp.ftplet.FtpException;
import com.hzgc.collect.service.ftp.listener.ListenerFactory;
import com.hzgc.collect.service.ftp.nativefs.filesystem.NativeFileSystemFactory;
import com.hzgc.collect.service.ftp.usermanager.PropertiesUserManagerFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Component
@Slf4j
public class FTP extends ClusterOverFtp implements Serializable {
    @Autowired
    @SuppressWarnings("unused")
    private CollectContext collectContext;

    @Autowired
    @SuppressWarnings("unused")
    private FtpServerFactory ftpServerFactory;

    @Autowired
    @SuppressWarnings("unused")
    private ListenerFactory listenerFactory;

    @Autowired
    @SuppressWarnings("unused")
    private PropertiesUserManagerFactory userManagerFactory;

    @Autowired
    @SuppressWarnings("unused")
    private CommandFactoryFactory commandFactoryFactory;

    @Autowired
    @SuppressWarnings("unused")
    private NativeFileSystemFactory nativeFileSystemFactory;
    
    @Autowired
    @SuppressWarnings("unused")
    private ConnectionConfigFactory connectionConfigFactory;


    @Override
    public void startFtpServer() {
        ftpServerFactory.createCustomContext(collectContext);
        //set the port of the listener
        listenerFactory.setPort(collectContext.getFtpPort());
        // replace the default listener
        ftpServerFactory.addListener("default", listenerFactory.createListener());
        // set customer user manager
        ftpServerFactory.setUserManager(userManagerFactory.createUserManager());
        //set customer cmd factory
        ftpServerFactory.setCommandFactory(commandFactoryFactory.createCommandFactory());
        //set local file system
        ftpServerFactory.setFileSystem(nativeFileSystemFactory);
        //set connection manager
        ftpServerFactory.setConnectionConfig(connectionConfigFactory.createUDConnectionConfig());
        //init collectContext
        collectContext.initAll();
        //create ftp server
        FtpServer server = ftpServerFactory.createServer();
        try {
            server.start();
        } catch (FtpException e) {
            e.printStackTrace();
        }
        //print ftp log
        log.info("\n" + collectContext.getLogo());

    }
}
