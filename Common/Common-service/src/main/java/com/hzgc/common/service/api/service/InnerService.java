package com.hzgc.common.service.api.service;

import com.hzgc.common.service.api.bean.UrlInfo;
import com.hzgc.common.service.rest.BigDataPath;
import com.hzgc.common.util.basic.StopWatch;
import com.hzgc.jniface.PictureData;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class InnerService {
    @Resource(name = "inner")
    @SuppressWarnings("unused")
    private RestTemplate restTemplate;

    public UrlInfo httpHostNameToIp(String httpHostName) {
        if (httpHostName != null && !"".equals(httpHostName)) {
            StopWatch stopWatch = new StopWatch();
            stopWatch.start();
            UrlInfo urlInfo = restTemplate.getForObject("http://collect" + BigDataPath.HTTP_HOSTNAME_TO_IP +
                    "?hostNameUrl=" + httpHostName, UrlInfo.class);
            stopWatch.stop();
            log.debug("Method httpHostNameToIp, request successfull, total time is:{}", stopWatch.getLastTaskTimeMillis());
            return urlInfo;
        } else {
            log.error("Method httpHostNameToIp, httpHostName is null");
            return new UrlInfo();
        }
    }

    public UrlInfo hostName2Ip(String hostName) {
        if (hostName != null && !"".equals(hostName)) {
            UrlInfo cacheUrlInfo = HostNameSingleton.getInstance().getHostNameInfo(hostName);
            if (cacheUrlInfo != null) {
                return cacheUrlInfo;
            } else {
                StopWatch stopWatch = new StopWatch();
                stopWatch.start();
                UrlInfo urlInfo = restTemplate.getForObject("http://collect" + BigDataPath.HOSTNAME_TO_IP +
                        "?hostName=" + hostName, UrlInfo.class);
                stopWatch.stop();
                log.info("Method hostName2Ip, request successfull, total time is:{}", stopWatch.getLastTaskTimeMillis());
                HostNameSingleton.getInstance().setHostNameInfo(hostName, urlInfo);
                return urlInfo;
            }

        } else {
            log.error("Method hostName2Ip, hostName is null");
            return new UrlInfo();
        }
    }

    public Map<String, UrlInfo> hostName2IpBatch(List<String> hostNameList) {
        if (hostNameList != null && hostNameList.size() > 0) {
            StopWatch stopWatch = new StopWatch();
            stopWatch.start();
            ParameterizedTypeReference<Map<String, UrlInfo>> parameterizedTypeReference =
                    new ParameterizedTypeReference<Map<String, UrlInfo>>() {};
            ResponseEntity<Map<String, UrlInfo>> responseEntity =
                    restTemplate.exchange("http://collect" + BigDataPath.HOSTNAME_TO_IP,
                            HttpMethod.POST,
                            new HttpEntity<>(hostNameList),
                            parameterizedTypeReference);
            stopWatch.stop();
            log.info("Method hostName2IpBatch, request successfull, total time is:{}", stopWatch.getLastTaskTimeMillis());
            return responseEntity.getBody();
        } else {
            log.error("Method hostName2IpBatch, hostNameList is null or size is 0");
            return new HashMap<>();
        }
    }

    public BigPictureData faceFeautreExtract(String base64Str) {
        if (base64Str != null && !"".equals(base64Str)) {
            StopWatch stopWatch = new StopWatch();
            stopWatch.start();
            ParameterizedTypeReference<PictureData> parameterizedTypeReference =
                    new ParameterizedTypeReference<PictureData>() {
                    };
            ResponseEntity<PictureData> data = restTemplate.exchange("http://collect-ftp" +
                            BigDataPath.FEATURE_EXTRACT_BASE64, HttpMethod.POST,
                    new HttpEntity<>(base64Str), parameterizedTypeReference);
            stopWatch.stop();
            log.info("Method faceFeatureExtract, request seccessull, total time is:{}", stopWatch.getTotalTimeMillis());
            return data.getBody();
        } else {
            log.error("Method faceFeatureExtract, base64Str is null or size is 0");
            return null;
        }
    }

    public PictureData faceFeautreCheck(String base64Str) {
        if (base64Str != null && !"".equals(base64Str)) {
            StopWatch stopWatch = new StopWatch();
            stopWatch.start();
            ParameterizedTypeReference<PictureData> parameterizedTypeReference =
                    new ParameterizedTypeReference<PictureData>() {
                    };
            ResponseEntity<PictureData> data = restTemplate.exchange("http://collect-ftp" +
                            BigDataPath.FEATURE_CHECK_BASE64, HttpMethod.POST,
                    new HttpEntity<>(base64Str), parameterizedTypeReference);
            stopWatch.stop();
            log.info("Method faceFeautreCheck, request seccessull, total time is:{}", stopWatch.getTotalTimeMillis());
            return data.getBody();
        } else {
            log.error("Method faceFeautreCheck, base64Str is null or size is 0");
            return null;
        }
    }
}

class HostNameSingleton {
    private Map<String, HostNameInfo> hostNameInfoMap = new ConcurrentHashMap<>();
    private static HostNameSingleton instance = new HostNameSingleton();

    private HostNameSingleton() {
    }

    public static HostNameSingleton getInstance() {
        return instance;
    }

    UrlInfo getHostNameInfo(String hostName) {
        if (hostName != null && hostName.length() > 0) {
            HostNameInfo hostNameInfo = hostNameInfoMap.get(hostName);
            if (hostNameInfo != null) {
                long currentTime = System.currentTimeMillis();
                if (currentTime - hostNameInfo.getTimeStamp() <= 15000) {
                    return hostNameInfo.getUrlInfo();
                } else {
                    return null;
                }
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    void setHostNameInfo(String hostName, UrlInfo urlInfo) {
        if (hostName != null && hostName.length() > 0 && urlInfo != null) {
            HostNameInfo hostNameInfo = new HostNameInfo();
            hostNameInfo.setHostName(hostName);
            hostNameInfo.setUrlInfo(urlInfo);
            hostNameInfo.setTimeStamp(System.currentTimeMillis());
            hostNameInfoMap.put(hostName, hostNameInfo);
        }
    }


    @Data
    private static class HostNameInfo {
        private String hostName;
        private long timeStamp;
        private UrlInfo urlInfo;
    }
}

