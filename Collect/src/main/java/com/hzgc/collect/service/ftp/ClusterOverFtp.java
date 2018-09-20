package com.hzgc.collect.service.ftp;

import java.io.Serializable;

public abstract class ClusterOverFtp implements Serializable {

    public void loadConfig() throws Exception {
        DataConnectionConfigurationFactory dataConnConf = new DataConnectionConfigurationFactory();
        //if (passivePorts != null) {
            //dataConnConf.setPassivePorts(passivePorts);
        //}
    }

    public abstract void startFtpServer();
}
