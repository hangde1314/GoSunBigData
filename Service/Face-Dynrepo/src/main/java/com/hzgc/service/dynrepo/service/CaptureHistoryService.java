package com.hzgc.service.dynrepo.service;

import com.hzgc.common.collect.facedis.FtpRegisterClient;
import com.hzgc.common.collect.util.ConverFtpurl;
import com.hzgc.common.service.facedynrepo.FaceTable;
import com.hzgc.common.util.json.JacksonUtil;
import com.hzgc.service.dynrepo.bean.CaptureOption;
import com.hzgc.service.dynrepo.bean.CapturedPicture;
import com.hzgc.service.dynrepo.bean.SingleCaptureResult;
import com.hzgc.service.dynrepo.bean.SortParam;
import com.hzgc.service.dynrepo.dao.ElasticSearchDao;
import com.hzgc.service.dynrepo.dao.EsSearchParam;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CaptureHistoryService {

    @Autowired
    private FtpRegisterClient ftpRegisterClient;
    @Autowired
    @SuppressWarnings("unused")
    private ElasticSearchDao elasticSearchDao;
    @Autowired
    @SuppressWarnings("unused")
    private Environment environment;
    @Autowired
    @SuppressWarnings("unused")
    private CaptureServiceHelper captureServiceHelper;
    @Value(value = "${ftp.port}")
    private String ftpPort;

    public List<SingleCaptureResult> getCaptureHistory(CaptureOption option) {
        String sortParam = EsSearchParam.DESC;
        List<SortParam> sortParams = option.getSort()
                .stream().map(param -> SortParam.values()[param]).collect(Collectors.toList());
        for (SortParam s : sortParams) {
            if (s.name().equals(SortParam.TIMEDESC.toString())) {
                sortParam = EsSearchParam.DESC;
            } else if (s.name().equals(SortParam.SIMDASC.toString())) {
                sortParam = EsSearchParam.ASC;
            }
        }

        if (sortParams.get(0).name().equals(SortParam.IPC.toString())) {
            log.info("The current query needs to be grouped by ipcid");
            return getCaptureHistory(option, sortParam);
        } else if (!sortParams.get(0).name().equals(SortParam.IPC.toString())) {
            log.info("The current query don't needs to be grouped by ipcid");
            return getCaptureHistory(option, option.getDeviceIpcs(), sortParam);
        } else {
            log.info("The current query is default");
            return getDefaultCaptureHistory(option, sortParam);
        }
    }

    private List<SingleCaptureResult> getDefaultCaptureHistory(CaptureOption option, String sortParam) {
        List<SingleCaptureResult> results = new ArrayList<>();
        SingleCaptureResult singleResult = new SingleCaptureResult();
        SearchResponse searchResponse = elasticSearchDao.getCaptureHistory(option, sortParam);
        SearchHits searchHits = searchResponse.getHits();
        SearchHit[] hits = searchHits.getHits();
        int totallCount = (int) searchHits.getTotalHits();
        List<CapturedPicture> persons = new ArrayList<>();
        CapturedPicture capturePicture;
        if (hits.length > 0) {
            for (SearchHit hit : hits) {
                capturePicture = new CapturedPicture();
                String sabsolutepath = (String) hit.getSource().get(FaceTable.SABSOLUTEPATH);
                String babsolutepath = (String) hit.getSource().get(FaceTable.BABSOLUTEPATH);
                String ipcid = (String) hit.getSource().get(FaceTable.IPCID);
                String timestamp = (String) hit.getSource().get(FaceTable.TIMESTAMP);
                String hostname = (String) hit.getSource().get(FaceTable.HOSTNAME);
                Map <String, String> ftpIpMapping = ftpRegisterClient.getFtpIpMapping();
                String ip = ftpIpMapping.get(ftpIpMapping.get(hostname));
                capturePicture.setSabsolutepath(ConverFtpurl.toHttpPath(ip,ftpPort,sabsolutepath));
                capturePicture.setBabsolutepath(ConverFtpurl.toHttpPath(ip,ftpPort,babsolutepath));
                capturePicture.setDeviceId(ipcid);
                capturePicture.setTimeStamp(timestamp);
                persons.add(capturePicture);
            }
        }
        singleResult.setTotal(totallCount);
        singleResult.setPictures(persons);
        results.add(singleResult);
        return results;
    }

    private List<SingleCaptureResult> getCaptureHistory(CaptureOption option, String sortParam) {
        List<SingleCaptureResult> results = new ArrayList<>();
        for (String ipcId : option.getDeviceIpcs()) {
            SingleCaptureResult singleResult = new SingleCaptureResult();
            List<CapturedPicture> capturedPictureList = new ArrayList<>();
            SearchResponse searchResponse = elasticSearchDao.getCaptureHistory(option, ipcId, sortParam);
            SearchHits searchHits = searchResponse.getHits();

            SearchHit[] hits = searchHits.getHits();
            CapturedPicture capturePicture;
            if (hits.length > 0) {
                for (SearchHit hit : hits) {
                    capturePicture = new CapturedPicture();
                    String sabsolutepath = (String) hit.getSource().get(FaceTable.SABSOLUTEPATH);
                    String babsolutepath = (String) hit.getSource().get(FaceTable.BABSOLUTEPATH);
                    String ipc = (String) hit.getSource().get(FaceTable.IPCID);
                    String timestamp = (String) hit.getSource().get(FaceTable.TIMESTAMP);
                    String hostname = (String) hit.getSource().get(FaceTable.HOSTNAME);
                    Map <String, String> ftpIpMapping = ftpRegisterClient.getFtpIpMapping();
                    String ip = ftpIpMapping.get(hostname);
                    capturePicture.setSabsolutepath(ConverFtpurl.toHttpPath(ip,ftpPort,sabsolutepath));
                    capturePicture.setBabsolutepath(ConverFtpurl.toHttpPath(ip,ftpPort,babsolutepath));
                    capturePicture.setDeviceId(option.getIpcMappingDevice().get(ipc).getId());
                    capturePicture.setDeviceName(option.getIpcMappingDevice().get(ipc).getName());
                    capturePicture.setTimeStamp(timestamp);
                    if (ipcId.equals(ipc)) {
                        capturedPictureList.add(capturePicture);
                    }
                }
            } else {
                capturePicture = new CapturedPicture();
                capturedPictureList.add(capturePicture);
            }
            singleResult.setTotal((int) searchHits.getTotalHits());
            singleResult.setDeviceId(option.getIpcMappingDevice().get(ipcId).getId());
            singleResult.setDeviceName(option.getIpcMappingDevice().get(ipcId).getName());
            singleResult.setPictures(capturedPictureList);
            results.add(singleResult);
        }
        log.info("Capture history results:" + JacksonUtil.toJson(results));
        return results;
    }

    private List<SingleCaptureResult> getCaptureHistory(CaptureOption option, List<String> deviceIds, String sortParam) {
        List<SingleCaptureResult> results = new ArrayList<>();
        SingleCaptureResult singleResult = new SingleCaptureResult();
        List<CapturedPicture> captureList = new ArrayList<>();

        SearchResponse searchResponse = elasticSearchDao.getCaptureHistory(option, deviceIds, sortParam);
        SearchHits searchHits = searchResponse.getHits();
        SearchHit[] hits = searchHits.getHits();
        CapturedPicture capturePicture;
        if (hits.length > 0) {
            for (SearchHit hit : hits) {
                capturePicture = new CapturedPicture();
                String sabsolutepath = (String) hit.getSource().get(FaceTable.SABSOLUTEPATH);
                String babsolutepath = (String) hit.getSource().get(FaceTable.BABSOLUTEPATH);
                String ipc = (String) hit.getSource().get(FaceTable.IPCID);
                String timestamp = (String) hit.getSource().get(FaceTable.TIMESTAMP);
                String hostname = (String) hit.getSource().get(FaceTable.HOSTNAME);
                Map <String, String> ftpIpMapping = ftpRegisterClient.getFtpIpMapping();
                String ip = ftpIpMapping.get(hostname);
                capturePicture.setSabsolutepath(ConverFtpurl.toHttpPath(ip,ftpPort,sabsolutepath));
                capturePicture.setBabsolutepath(ConverFtpurl.toHttpPath(ip,ftpPort,babsolutepath));
                capturePicture.setDeviceId(ipc);
                capturePicture.setTimeStamp(timestamp);
                capturePicture.setDeviceId(option.getIpcMappingDevice().get(ipc).getId());
                capturePicture.setDeviceName(option.getIpcMappingDevice().get(ipc).getName());
                captureList.add(capturePicture);
            }
        }
        singleResult.setTotal((int) searchHits.getTotalHits());
        singleResult.setPictures(captureList);
        singleResult.setDeviceId(option.getDeviceIds().get(0).toString());
        singleResult.setDeviceName(option.getIpcMappingDevice().get(option.getDeviceIpcs().get(0)).getName());
        results.add(singleResult);
        return results;
    }
}