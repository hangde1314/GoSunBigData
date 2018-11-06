package com.hzgc.service.dyncar.service;

import com.hzgc.common.collect.util.CollectUrlUtil;
import com.hzgc.common.service.api.bean.CameraQueryDTO;
import com.hzgc.common.service.api.bean.UrlInfo;
import com.hzgc.common.service.api.service.InnerService;
import com.hzgc.common.service.api.service.PlatformService;
import com.hzgc.common.service.facedynrepo.VehicleTable;
import com.hzgc.common.util.basic.UuidUtil;
import com.hzgc.common.util.json.JacksonUtil;
import com.hzgc.jniface.CarAttribute;
import com.hzgc.service.dyncar.bean.*;
import com.hzgc.service.dyncar.dao.ElasticSearchDao;
import com.hzgc.service.dyncar.dao.EsSearchParam;
import com.hzgc.service.dyncar.util.DeviceToIpcs;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class CaptureHistoryService {

    @Autowired
    @SuppressWarnings("unused")
    private ElasticSearchDao elasticSearchDao;
    @Autowired
    private PlatformService platformService;
    @Autowired
    private InnerService innerService;

    public SearchResult getCaptureHistory(CaptureOption option) {
        String sortParam = EsSearchParam.DESC;
        log.info("The current query don't needs to be grouped by ipcid");
        return getCaptureHistory(option, DeviceToIpcs.getIpcs(option.getDevices()), sortParam);
    }

    //根据ipcid进行分类
    private SearchResult getCaptureHistory(CaptureOption option, String sortParam) {
        SearchResult searchResult = new SearchResult();
        List<GroupByIpc> groupByIpcs = new ArrayList<>();
        List<String> ipcs = DeviceToIpcs.getIpcs(option.getDevices());
        SingleSearchResult singleSearchResult = new SingleSearchResult();
        for (String ipcId : ipcs) {
            List<CapturedPicture> capturedPictureList = new ArrayList<>();
            SearchResponse searchResponse = elasticSearchDao.getCaptureHistory(option, ipcId, sortParam);
            SearchHits searchHits = searchResponse.getHits();
            GroupByIpc groupByIpc = new GroupByIpc();
            SearchHit[] hits = searchHits.getHits();
            CapturedPicture capturePicture;
            if (hits.length > 0) {
                for (SearchHit hit : hits) {
                    capturePicture = new CapturedPicture();
                    String sabsolutepath = (String) hit.getSource().get(VehicleTable.SABSOLUTEPATH);
                    String babsolutepath = (String) hit.getSource().get(VehicleTable.BABSOLUTEPATH);
                    String ipc = (String) hit.getSource().get(VehicleTable.IPCID);
                    String timestamp = (String) hit.getSource().get(VehicleTable.TIMESTAMP);
                    String hostname = (String) hit.getSource().get(VehicleTable.HOSTNAME);
//                    Map<String, String> ftpIpMapping = ftpRegisterClient.getFtpIpMapping();
//                    String ip = ftpIpMapping.get(hostname);
                    //参数封装
                    CarAttribute carAttribute = carDataPackage(hit);
                    capturePicture.setCarAttribute(carAttribute);
//                    capturePicture.setSabsolutepath(ConverFtpurl.toHttpPath(ip, ftpPort, sabsolutepath));
//                    capturePicture.setBabsolutepath(ConverFtpurl.toHttpPath(ip, ftpPort, babsolutepath));
                    capturePicture.setBabsolutepath(babsolutepath);
                    capturePicture.setSabsolutepath(sabsolutepath);
                    capturePicture.setDeviceId(ipc);
                    capturePicture.setDeviceName(option.getIpcMapping().get(ipc).getDeviceName());
                    capturePicture.setTimestamp(timestamp);
                    if (ipcId.equals(ipc)) {
                        capturedPictureList.add(capturePicture);
                    }
                }
                groupByIpc.setPictures(capturedPictureList);
                groupByIpc.setDeviceId(ipcId);
                groupByIpc.setTotal(capturedPictureList.size());
                groupByIpc.setDeviceName(option.getIpcMapping().get(ipcId).getDeviceName());
                groupByIpcs.add(groupByIpc);
                singleSearchResult.setTotal(groupByIpcs.size());
            }
        }
        singleSearchResult.setDevicePictures(groupByIpcs);
        searchResult.setSingleSearchResult(singleSearchResult);
        searchResult.setSearchId(UuidUtil.getUuid());
        log.info("Capture history results:" + JacksonUtil.toJson(searchResult));
        return searchResult;
    }

    //默认查询，不进行设别分类
    private SearchResult getDefaultCaptureHistory(CaptureOption option, String sortParam) {
        SearchResponse searchResponse = elasticSearchDao.getCaptureHistory(option, sortParam);
        SearchHits searchHits = searchResponse.getHits();
        SearchHit[] hits = searchHits.getHits();
        SearchResult searchResult = new SearchResult();
        SingleSearchResult singleSearchResult = new SingleSearchResult();
        List<CapturedPicture> pictures = new ArrayList<>();
        CapturedPicture capturePicture;
        if (hits.length > 0) {
            for (SearchHit hit : hits) {
                capturePicture = new CapturedPicture();
                String sabsolutepath = (String) hit.getSource().get(VehicleTable.SABSOLUTEPATH);
                String babsolutepath = (String) hit.getSource().get(VehicleTable.BABSOLUTEPATH);
                String ipcid = (String) hit.getSource().get(VehicleTable.IPCID);
                String timestamp = (String) hit.getSource().get(VehicleTable.TIMESTAMP);
                String hostname = (String) hit.getSource().get(VehicleTable.HOSTNAME);
//                Map<String, String> ftpIpMapping = ftpRegisterClient.getFtpIpMapping();
//                String ip = ftpIpMapping.get(hostname);
                //参数封装
                CarAttribute carAttribute = carDataPackage(hit);
                capturePicture.setCarAttribute(carAttribute);
//                capturePicture.setSabsolutepath(ConverFtpurl.toHttpPath(ip, ftpPort, sabsolutepath));
//                capturePicture.setBabsolutepath(ConverFtpurl.toHttpPath(ip, ftpPort, babsolutepath));
                capturePicture.setDeviceId(ipcid);
                capturePicture.setDeviceName(option.getIpcMapping().get(ipcid).getDeviceName());
                capturePicture.setTimestamp(timestamp);
                pictures.add(capturePicture);
            }
        }
        singleSearchResult.setSearchId(UuidUtil.getUuid());
        singleSearchResult.setPictures(pictures);
        singleSearchResult.setTotal(pictures.size());
        searchResult.setSingleSearchResult(singleSearchResult);
        searchResult.setSearchId(UuidUtil.getUuid());
        return searchResult;
    }

    //多个ipcid查询
    private SearchResult getCaptureHistory(CaptureOption option, List<String> deviceIds, String sortParam) {
        SearchResult searchResult = new SearchResult();
        SingleSearchResult singleSearchResult = new SingleSearchResult();
        List<CapturedPicture> captureList = new ArrayList<>();
        SearchResponse searchResponse = elasticSearchDao.getCaptureHistory(option, deviceIds, sortParam);
        SearchHits searchHits = searchResponse.getHits();
        SearchHit[] hits = searchHits.getHits();
        CapturedPicture capturePicture;
        if (hits.length > 0) {
            for (SearchHit hit : hits) {
                capturePicture = new CapturedPicture();
                String sabsolutepath = (String) hit.getSource().get(VehicleTable.SABSOLUTEPATH);
                log.info("sabsolutepath : " + sabsolutepath);
                String babsolutepath = (String) hit.getSource().get(VehicleTable.BABSOLUTEPATH);
                String ipc = (String) hit.getSource().get(VehicleTable.IPCID);
                String timestamp = (String) hit.getSource().get(VehicleTable.TIMESTAMP);
                String hostname = (String) hit.getSource().get(VehicleTable.HOSTNAME);
                UrlInfo urlInfo = innerService.hostName2Ip(hostname);
                capturePicture.setSabsolutepath(CollectUrlUtil.toHttpPath(urlInfo.getIp(), urlInfo.getPort(), sabsolutepath));
                capturePicture.setBabsolutepath(CollectUrlUtil.toHttpPath(urlInfo.getIp(), urlInfo.getPort(), babsolutepath));
                //参数封装
                CarAttribute carAttribute = carDataPackage(hit);
                capturePicture.setCarAttribute(carAttribute);
                capturePicture.setDeviceId(ipc);
                capturePicture.setTimestamp(timestamp);
                capturePicture.setDeviceId(ipc);
                capturePicture.setDeviceName(option.getIpcMapping().get(ipc).getDeviceName());
                capturePicture.setLocation(getLocation(ipc));
                captureList.add(capturePicture);
            }
        }
        singleSearchResult.setDeviceTotal(option.getIpcMapping().entrySet().size());
        singleSearchResult.setTotal((int) searchHits.getTotalHits());
        singleSearchResult.setPictures(captureList);
        singleSearchResult.setSearchId(UuidUtil.getUuid());
        searchResult.setSingleSearchResult(singleSearchResult);
        log.info("**************" + JacksonUtil.toJson(searchResult));
        return searchResult;
    }

    private String getLocation(String ipc) {
        //查询相机位置
        ArrayList<String> list = new ArrayList<>();
        list.add(ipc);
        Map<String, CameraQueryDTO> cameraInfoByBatchIpc = platformService.getCameraInfoByBatchIpc(list);
        CameraQueryDTO cameraQueryDTO = cameraInfoByBatchIpc.get(ipc);
        return cameraQueryDTO.getRegion() + cameraQueryDTO.getCommunity();
    }

    //数据封装
    private static CarAttribute carDataPackage(SearchHit hit) {
        return new CarAttribute((String) hit.getSource().get("vehicle_object_type"),
                (String) hit.getSource().get("belt_maindriver"),
                (String) hit.getSource().get("belt_codriver"),
                (String) hit.getSource().get("brand_name"),
                (String) hit.getSource().get("call_code"),
                (String) hit.getSource().get("vehicle_color"),
                (String) hit.getSource().get("crash_code"),
                (String) hit.getSource().get("danger_code"),
                (String) hit.getSource().get("marker_code"),
                (String) hit.getSource().get("plate_schelter_code"),
                (String) hit.getSource().get("plate_flag_code"),
                (String) hit.getSource().get("plate_licence"),
                (String) hit.getSource().get("plate_destain_code"),
                (String) hit.getSource().get("plate_color_code"),
                (String) hit.getSource().get("plate_type_code"),
                (String) hit.getSource().get("rack_code"),
                (String) hit.getSource().get("sparetire_code"),
                (String) hit.getSource().get("mistake_code"),
                (String) hit.getSource().get("sunroof_code"),
                (String) hit.getSource().get("vehicle_type"));

    }
}