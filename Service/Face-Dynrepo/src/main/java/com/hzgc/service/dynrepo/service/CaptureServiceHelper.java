package com.hzgc.service.dynrepo.service;

import com.hzgc.common.collect.facedis.FtpRegisterClient;
import com.hzgc.common.service.facedynrepo.FaceTable;
import com.hzgc.common.service.api.bean.DeviceDTO;
import com.hzgc.common.service.api.service.DeviceQueryService;
import com.hzgc.common.util.basic.IsEmpty;
import com.hzgc.jniface.PictureData;
import com.hzgc.service.dynrepo.bean.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

/**
 * 动态库实现类
 */
@Component
@Slf4j
public class CaptureServiceHelper {

    @Autowired
    @SuppressWarnings("unused")
    private Environment environment;

    @Autowired
    @SuppressWarnings("unused")
    private DeviceQueryService queryService;

    @Autowired
    @SuppressWarnings("unused")
    private FtpRegisterClient register;

    /**
     * 通过排序参数进行排序
     *
     * @param result 查询结果
     * @param option 查询结果的查询参数
     */
    void sortByParamsAndPageSplit(SearchResult result, SearchResultOption option) {
        List<Integer> paramListInt = option.getSort();
        List<SortParam> paramList = paramListInt.stream().map(param -> SortParam.values()[param]).collect(toList());
        List<Boolean> isAscArr = new ArrayList<>();
        List<String> sortNameArr = new ArrayList<>();
        for (SortParam aParamList : paramList) {
            switch (aParamList) {
                case TIMEASC:
                    isAscArr.add(true);
                    sortNameArr.add("timeStamp");
                    break;
                case TIMEDESC:
                    isAscArr.add(false);
                    sortNameArr.add("timeStamp");
                    break;
                case SIMDESC:
                    isAscArr.add(false);
                    sortNameArr.add("similarity");
                    break;
                case SIMDASC:
                    isAscArr.add(true);
                    sortNameArr.add("similarity");
                    break;
            }
        }
        if (paramList.contains(SortParam.IPC)) {
            groupByIpc(result);
            for (SingleSearchResult singleResult : result.getSingleResults()) {
                for (GroupByIpc groupByIpc : singleResult.getDevicePictures()) {
                    CapturePictureSortUtil.sort(groupByIpc.getPictures(), sortNameArr, isAscArr);
                    groupByIpc.setPictures(pageSplit(groupByIpc.getPictures(), option));
                }
                singleResult.setPictures(null);
            }
        } else {
            for (SingleSearchResult singleResult : result.getSingleResults()) {
                CapturePictureSortUtil.sort(singleResult.getPictures(), sortNameArr, isAscArr);
                singleResult.setPictures(pageSplit(singleResult.getPictures(), option));
            }
        }
    }

    /**
     * 根据设备ID进行归类
     *
     * @param result 历史查询结果
     */
    private void groupByIpc(SearchResult result) {
        for (SingleSearchResult singleResult : result.getSingleResults()) {
            List<GroupByIpc> list = new ArrayList<>();
            Map<String, List<CapturedPicture>> map =
                    singleResult.getPictures().stream().collect(Collectors.groupingBy(CapturedPicture::getDeviceId));
            for (String key : map.keySet()) {
                GroupByIpc groupByIpc = new GroupByIpc();
                groupByIpc.setDeviceId(key);
                groupByIpc.setPictures(map.get(key));
                groupByIpc.setTotal(map.get(key).size());
                list.add(groupByIpc);
            }
            singleResult.setDevicePictures(list);
        }
    }

    /**
     * 对图片对象列表进行分页返回
     *
     * @param capturedPictures 待分页的图片对象列表
     * @param option           查询结果的查询参数
     * @return 返回分页查询结果
     */
    List<CapturedPicture> pageSplit(List<CapturedPicture> capturedPictures, SearchResultOption option) {
        int offset = option.getStart();
        int count = option.getLimit();
        List<CapturedPicture> subCapturePictureList;
        int totalPicture = capturedPictures.size();
        if (offset > -1 && totalPicture > (offset + count - 1)) {
            //结束行小于总数，取起始行开始后续count条数据
            subCapturePictureList = capturedPictures.subList(offset, offset + count);
        } else {
            //结束行大于总数，则返回起始行开始的后续所有数据
            subCapturePictureList = capturedPictures.subList(offset, totalPicture);
        }
        return subCapturePictureList;
    }

    List<CapturedPicture> pageSplit(List<CapturedPicture> capturedPictures, int offset, int count) {
        List<CapturedPicture> subCapturePictureList;
        int totalPicture = capturedPictures.size();
        if (offset >= 0 && totalPicture > (offset + count - 1) && count > 0) {
            //结束行小于总数，取起始行开始后续count条数据
            subCapturePictureList = capturedPictures.subList(offset, offset + count);
        } else {
            //结束行大于总数，则返回起始行开始的后续所有数据
            subCapturePictureList = capturedPictures.subList(offset, totalPicture);
        }
        return subCapturePictureList;
    }

    SearchResult parseResultOnePerson(ResultSet resultSet, SearchOption option, String searchId) {
        SingleSearchResult singleSearchResult = new SingleSearchResult();
        SearchResult searchResult = new SearchResult();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        List<CapturedPicture> capturedPictureList = new ArrayList<>();
        try {
            while (resultSet.next()) {
                //小图ftpurl
                String surl = resultSet.getString(FaceTable.FTPURL);
                //设备id
                String ipcid = resultSet.getString(FaceTable.IPCID);
                //相似度
                Float similaritys = resultSet.getFloat(FaceTable.SIMILARITY);
                //时间戳
                Timestamp timestamp = resultSet.getTimestamp(FaceTable.TIMESTAMP);
                //大图ftpurl
                String burl = surlToBurl(surl);
                //图片对象
                CapturedPicture capturedPicture = new CapturedPicture();
                capturedPicture.setSurl(getFtpUrl(surl));
                capturedPicture.setBurl(getFtpUrl(burl));
                capturedPicture.setDeviceId(option.getIpcMappingDevice().get(ipcid).getId());
                capturedPicture.setDeviceName(option.getIpcMappingDevice().get(ipcid).getName());
                capturedPicture.setTimeStamp(format.format(timestamp));
                capturedPicture.setSimilarity(similaritys);
                capturedPictureList.add(capturedPicture);
            }

            singleSearchResult.setPictureDatas(option.getImages());
            singleSearchResult.setSearchId(searchId);
            singleSearchResult.setPictures(capturedPictureList);
            singleSearchResult.setTotal(capturedPictureList.size());
            searchResult.setSearchId(searchId);
            List<SingleSearchResult> singleList = new ArrayList<>();
            singleList.add(singleSearchResult);
            searchResult.setSingleResults(singleList);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return searchResult;
    }

    SearchResult parseResultNotOnePerson(ResultSet resultSet, SearchOption option, String searchId) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Map<String, List<CapturedPicture>> mapSet = new HashMap<>();
        SearchResult searchResult = new SearchResult();
        List<SingleSearchResult> singleResultList = new ArrayList<>();
        try {
            while (resultSet.next()) {
                //小图ftpurl
                String surl = resultSet.getString(FaceTable.FTPURL);
                //设备id
                String ipcid = resultSet.getString(FaceTable.IPCID);
                //相似度
                Float similaritys = resultSet.getFloat(FaceTable.SIMILARITY);
                //时间戳
                Timestamp timestamp = resultSet.getTimestamp(FaceTable.TIMESTAMP);
                //group id
                String id = resultSet.getString(FaceTable.GROUP_FIELD);
                //大图ftpurl
                String burl = surlToBurl(surl);
                //图片对象
                CapturedPicture capturedPicture = new CapturedPicture();
                capturedPicture.setSurl(getFtpUrl(surl));
                capturedPicture.setBurl(getFtpUrl(burl));
                capturedPicture.setDeviceId(option.getIpcMappingDevice().get(ipcid).getId());
                capturedPicture.setDeviceName(option.getIpcMappingDevice().get(ipcid).getName());
                capturedPicture.setTimeStamp(format.format(timestamp));
                capturedPicture.setSimilarity(similaritys);
                if (mapSet.containsKey(id)) {
                    mapSet.get(id).add(capturedPicture);
                } else {
                    List<CapturedPicture> pictureList = new ArrayList<>();
                    pictureList.add(capturedPicture);
                    mapSet.put(id, pictureList);
                }
            }
            searchResult.setSearchId(searchId);
            for (int i = 0; i < option.getImages().size(); i++) {
                SingleSearchResult singleSearchResult = new SingleSearchResult();
                String picId = option.getImages().get(i).getImageID();
                if (mapSet.containsKey(picId)) {
                    singleSearchResult.setPictures(mapSet.get(picId));
                    singleSearchResult.setTotal(mapSet.get(picId).size());
                    List<PictureData> list = new ArrayList<>();
                    list.add(option.getImages().get(i));
                    singleSearchResult.setPictureDatas(list);
                    singleSearchResult.setSearchId(picId);
                    singleResultList.add(singleSearchResult);
                }
            }
            searchResult.setSingleResults(singleResultList);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return searchResult;
    }

    /**
     * ftpUrl中的HostName转为IP
     *
     * @param ftpUrl 带HostName的ftpUrl
     * @return 带IP的ftpUrl
     */
    String getFtpUrl(String ftpUrl) {

        String hostName = ftpUrl.substring(ftpUrl.indexOf("/") + 2, ftpUrl.lastIndexOf(":"));
        String ftpServerIP = register.getFtpIpMapping().get(hostName);
        if (IsEmpty.strIsRight(ftpServerIP)) {
            return ftpUrl.replace(hostName, ftpServerIP);
        }
        return ftpUrl;
    }

    /**
     * 将设备树中获取的设备ID转为ipc id
     *
     * @param idList 设备ID列表
     * @return ipc id列表
     */
    List<String> deviceIdToDeviceIpc(List<Long> idList) {
        Map<String, DeviceDTO> result = queryService.getDeviceInfoByBatchId(idList);
        return result.values().stream().map(DeviceDTO::getSerial).collect(toList());
    }

    /**
     * 向CaptureOption中添加转换后的IPC列表以及IPC映射DeviceDTO
     *
     * @param option 抓拍查询参数
     */
    public void capturOptionConver(CaptureOption option) {
        List<String> ipcList;
        Map<String, DeviceDTO> ipcListMapping;
        Map<String, DeviceDTO> result = queryService.getDeviceInfoByBatchId(option.getDeviceIds());
        if (!result.values().isEmpty() && result.values().size() > 0) {
            ipcListMapping = result.values().stream().collect(Collectors.toMap(DeviceDTO::getSerial, DeviceDTO -> DeviceDTO));
            ipcList = result.values().stream().map(DeviceDTO::getSerial).collect(toList());
        } else {
            log.error("Failed to find device id");
            ipcListMapping = new HashMap<>();
            ipcList = new ArrayList<>();
        }
        option.setDeviceIpcs(ipcList);
        option.setIpcMappingDevice(ipcListMapping);
    }

    /**
     * 设备id转ipcId
     *
     * @param deviceId 设备id
     * @return ipcId
     */
    public String deviceIdToIpcId(String deviceId) {
        List<Long> devices = new ArrayList<>();
        devices.add(Long.valueOf(deviceId));
        Map<String, DeviceDTO> result = queryService.getDeviceInfoByBatchId(devices);
        if (!result.values().isEmpty() && result.values().size() > 0) {
            return result.get(deviceId).getSerial();
        } else {
            log.error("Failed to find device id");
        }
        return null;
    }

    /**
     * 小图ftpUrl转大图ftpUrl
     *
     * @param surl 小图ftpUrl
     * @return 大图ftpUrl
     */
    String surlToBurl(String surl) {
        if (surl.contains("IPC-HFW5238M-AS-I1") || surl.contains("IPC-HDBW5238R-AS")){
            String frontStr = surl.substring(0, surl.lastIndexOf("[") + 1);
            String backStr = surl.substring(surl.lastIndexOf("[") + 2, surl.length());
            return frontStr + 0 + backStr;
        } else {
            StringBuilder burl = new StringBuilder();
            String s1 = surl.substring(0, surl.lastIndexOf("_") + 1);
            String s2 = surl.substring(surl.lastIndexOf("."));
            burl.append(s1).append(0).append(s2);
            return burl.toString();
        }
    }
}

