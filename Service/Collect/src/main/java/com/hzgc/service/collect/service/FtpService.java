package com.hzgc.service.collect.service;

import com.hzgc.common.collect.facedis.FtpRegisterClient;
import com.hzgc.common.collect.facesub.FtpSubscribeClient;
import com.hzgc.common.collect.util.CollectUrlUtil;
import com.hzgc.common.service.api.bean.UrlInfo;
import com.hzgc.service.collect.model.FtpInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class FtpService implements Serializable {
    @Autowired
    private FtpRegisterClient register;

    @Autowired
    private FtpSubscribeClient subscribe;
//    @Autowired
    private FtpInfo ftpInfo;
    private String getIPAddress(String hostname) {
        String ip = register.getFtpIpMapping().get(hostname);
//        String ip = ftpInfo.getFtpHome();
            if (ip != null) {
                return ip;
            } else {
                log.warn("HostName:{} is no found", hostname);
            }
        return null;
    }

    public Map<String, UrlInfo> hostName2IpBatch(List<String> hostNameList) {
        Map<String, UrlInfo> result = new HashMap<>();
        for (String  hostName: hostNameList) {
            UrlInfo urlInfo = new UrlInfo();
            urlInfo.setHostname(hostName);
            urlInfo.setHttp_ip(getIPAddress(hostName));
            result.put(hostName, urlInfo);
        }
        return result;
    }

    public boolean openFtpSubscription(String sessionId, List<String> ipcIdList) {
        if (!sessionId.equals("") && !ipcIdList.isEmpty()) {
            subscribe.updateSessionPath(sessionId, ipcIdList);
            return true;
        }
        return false;
    }

    public boolean closeFtpSubscription(String sessionId) {
        if (!sessionId.equals("")) {
            subscribe.deleteSessionPath(sessionId);
            return true;
        }
        return false;
    }

    public UrlInfo http_hostName2Ip(String hostNameUrl) {
        String ipUlr = CollectUrlUtil.httpHostNameToIp(hostNameUrl, register.getFtpIpMapping());
        UrlInfo urlInfo = new UrlInfo();
        urlInfo.setHttp_ip(ipUlr);
        urlInfo.setHttp_hostname(hostNameUrl);
        return urlInfo;
    }

    public UrlInfo hostName2Ip(String hostName) {
        UrlInfo urlInfo = new UrlInfo();
        urlInfo.setHostname(hostName);
        urlInfo.setIp(getIPAddress(hostName));
        return urlInfo;
    }
}

