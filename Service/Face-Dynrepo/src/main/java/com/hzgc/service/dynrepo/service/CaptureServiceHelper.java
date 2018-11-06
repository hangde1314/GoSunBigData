package com.hzgc.service.dynrepo.service;

import com.hzgc.common.collect.util.CollectUrlUtil;
import com.hzgc.common.service.api.bean.CameraQueryDTO;
import com.hzgc.common.service.api.bean.UrlInfo;
import com.hzgc.common.service.api.service.InnerService;
import com.hzgc.common.service.api.service.PlatformService;
import com.hzgc.common.service.facedynrepo.FaceTable;
import com.hzgc.jniface.PictureData;
import com.hzgc.service.dynrepo.bean.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;
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
    private PlatformService queryService;

    @Autowired
    @SuppressWarnings("unused")
    private InnerService innerService;

    @Autowired
    @SuppressWarnings("unused")
    private PlatformService platformService;

    private SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * 通过排序参数进行排序
     *
     * @param result 查询结果
     * @param option 查询结果的查询参数
     */
    SearchResult sortByParamsAndPageSplit(SearchResult result, SearchResultOption option) {
        List<Integer> paramListInt = option.getSort();
        List<SortParam> paramList = paramListInt.stream().map(param -> SortParam.values()[param]).collect(toList());
        List<Boolean> isAscArr = new ArrayList<>();
        List<String> sortNameArr = new ArrayList<>();
        SearchResult searchResult = new SearchResult();
        List<SingleSearchResult> singleSearchResults = new ArrayList<>();
        searchResult.setSearchId(result.getSearchId());
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
                SingleSearchResult tempSingleResult = new SingleSearchResult();
                List<CapturedPicture> pageList = pageSplit(singleResult.getPictures(), option);
                tempSingleResult.setPictures(pageList);
                tempSingleResult.setTotal(singleResult.getTotal());
                singleSearchResults.add(tempSingleResult);
            }
            searchResult.setSingleResults(singleSearchResults);
        }
        return searchResult;
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
        List<CapturedPicture> capturedPictureList = new ArrayList<>();
        Map<String, CameraQueryDTO> cameraQueryDTOMap = platformService.getCameraInfoByBatchIpc(
                option.getDeviceIpcs().stream().map(Device::getIpc).collect(toList()));
        try {
            while (resultSet.next()) {
                //设备id
                String ipcid = resultSet.getString(FaceTable.IPCID);
                //相似度
                Float similaritys = resultSet.getFloat(FaceTable.SIMILARITY);
                //时间戳
                Timestamp timestamp = resultSet.getTimestamp(FaceTable.TIMESTAMP);
                //大图路径
                String babsolutepath = resultSet.getString(FaceTable.BABSOLUTEPATH);
                //小图路径
                String sabsolutepath = resultSet.getString(FaceTable.SABSOLUTEPATH);
                String hostname = resultSet.getString(FaceTable.HOSTNAME);
                int gender = resultSet.getInt(FaceTable.GENDER);
                int age = resultSet.getInt(FaceTable.AGE);
                int huzi = resultSet.getInt(FaceTable.HUZI);
                int mask = resultSet.getInt(FaceTable.MASK);
                int eyeglasses = resultSet.getInt(FaceTable.EYEGLASSES);
                int sharpness = resultSet.getInt(FaceTable.SHARPNESS);
                //图片对象
                CapturedPicture capturedPicture = new CapturedPicture();
                UrlInfo urlInfo = innerService.hostName2Ip(hostname);
                capturedPicture.setSabsolutepath(CollectUrlUtil.toHttpPath(urlInfo.getIp(), urlInfo.getPort(), sabsolutepath));
                capturedPicture.setBabsolutepath(CollectUrlUtil.toHttpPath(urlInfo.getIp(), urlInfo.getPort(), babsolutepath));
                String ipcId = option.getIpcMapping().get(ipcid).getIpc();
                capturedPicture.setDeviceId(ipcId);
                CameraQueryDTO cameraInfo = cameraQueryDTOMap.get(ipcId);
                if (cameraInfo != null) {
                    capturedPicture.setLocation(cameraInfo.getRegion() + cameraInfo.getCommunity());
                    capturedPicture.setDeviceName(option.getIpcMapping().get(ipcid).getDeviceName());
                } else {
                    log.warn("Ipc id:{} is not found from platform service");
                }
                capturedPicture.setTimeStamp(format.format(timestamp));
                capturedPicture.setSimilarity(similaritys);
                capturedPicture.setAge(age);
                capturedPicture.setGender(gender);
                capturedPicture.setHuzi(huzi);
                capturedPicture.setMask(mask);
                capturedPicture.setEyeglasses(eyeglasses);
                capturedPicture.setSharpness(sharpness);
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
            log.error(e.getMessage());
        }
        return searchResult;
    }

    SearchResult parseResultNotOnePerson(ResultSet resultSet, SearchOption option, String searchId) {
        SearchResult searchResult = new SearchResult();
        Map<String, List<CapturedPicture>> mapSet = new HashMap<>();
        List<SingleSearchResult> singleResultList = new ArrayList<>();
        try {
            while (resultSet.next()) {
                //设备id
                String ipcid = resultSet.getString(FaceTable.IPCID);
                //相似度
                Float similaritys = resultSet.getFloat(FaceTable.SIMILARITY);
                //时间戳
                Timestamp timestamp = resultSet.getTimestamp(FaceTable.TIMESTAMP);
                //大图路径
                String babsolutepath = resultSet.getString(FaceTable.BABSOLUTEPATH);
                //小图路径
                String sabsolutepath = resultSet.getString(FaceTable.SABSOLUTEPATH);
                //hostname
                String hostname = resultSet.getString(FaceTable.HOSTNAME);
                //picture gourp id
                String picid = resultSet.getString(FaceTable.GROUP_FIELD);
                int gender = resultSet.getInt(FaceTable.GENDER);
                int age = resultSet.getInt(FaceTable.AGE);
                int huzi = resultSet.getInt(FaceTable.HUZI);
                int mask = resultSet.getInt(FaceTable.MASK);
                int eyeglasses = resultSet.getInt(FaceTable.EYEGLASSES);
                int sharpness = resultSet.getInt(FaceTable.SHARPNESS);
                //图片对象
                CapturedPicture capturedPicture = new CapturedPicture();
                UrlInfo urlInfo = innerService.hostName2Ip(hostname);
                capturedPicture.setSabsolutepath(CollectUrlUtil.toHttpPath(urlInfo.getIp(), urlInfo.getPort(), sabsolutepath));
                capturedPicture.setBabsolutepath(CollectUrlUtil.toHttpPath(urlInfo.getIp(), urlInfo.getPort(), babsolutepath));
                capturedPicture.setDeviceId(option.getIpcMapping().get(ipcid).getIpc());
                String ipcId = option.getIpcMapping().get(ipcid).getIpc();
                capturedPicture.setDeviceId(ipcId);
                CameraQueryDTO cameraInfo = platformService.getCameraInfoByBatchIpc(Collections.singletonList(ipcId)).get(ipcId);
                if (cameraInfo != null) {
                    capturedPicture.setLocation(cameraInfo.getRegion() + cameraInfo.getCommunity());
                    capturedPicture.setDeviceName(option.getIpcMapping().get(ipcid).getDeviceName());
                } else {
                    log.warn("Ipc id:{} is not found from platform service");
                }
                capturedPicture.setTimeStamp(format.format(timestamp));
                capturedPicture.setSimilarity(similaritys);
                capturedPicture.setGender(gender);
                capturedPicture.setAge(age);
                capturedPicture.setHuzi(huzi);
                capturedPicture.setMask(mask);
                capturedPicture.setEyeglasses(eyeglasses);
                capturedPicture.setSharpness(sharpness);
                if (mapSet.containsKey(picid)) {
                    mapSet.get(picid).add(capturedPicture);
                } else {
                    List<CapturedPicture> pictureList = new ArrayList<>();
                    pictureList.add(capturedPicture);
                    mapSet.put(picid, pictureList);
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
}

