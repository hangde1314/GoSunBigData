package com.hzgc.cloud.dynrepo.service;

import com.hzgc.common.util.json.JacksonUtil;
import com.hzgc.compare.CompareParam;
import com.hzgc.compare.Feature;
import com.hzgc.jniface.PictureData;
import com.hzgc.cloud.dynrepo.bean.*;
import com.hzgc.cloud.dynrepo.dao.ElasticSearchDao;
import com.hzgc.cloud.dynrepo.dao.FaceCompareClient;
import com.hzgc.cloud.dynrepo.dao.MemoryDao;
import com.hzgc.cloud.dynrepo.dao.SparkJDBCDao;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@Slf4j
public class CaptureSearchService {
    @Autowired
    @SuppressWarnings("unused")
    private SparkJDBCDao sparkJDBCDao;
    @Autowired
    @SuppressWarnings("unused")
    private ElasticSearchDao esDao;
    @Autowired
    @SuppressWarnings("unused")
    private CaptureServiceHelper captureServiceHelper;
    @Autowired
    @SuppressWarnings("unused")
    private MemoryDao memoryDao;

    @Autowired
    private FaceCompareClient client;

    public SearchResult searchPicture(SearchOption option, String searchId) throws SQLException {
        SearchResult searchResult;
        SearchResult retrunResult = new SearchResult();
        if (option.getDeviceIpcs() != null && option.getDeviceIpcs().size() > 0) {
            retrunResult.setDeivceCount(option.getDeviceIpcs().size());
        }
        ResultSet resultSet;
        long start = System.currentTimeMillis();
        SearchCallBack searchCallBack = sparkJDBCDao.searchPicture(option);
        log.info("Start search picture, execute query total time is:" + (System.currentTimeMillis() - start));
        resultSet = searchCallBack.getResultSet();
        if (resultSet != null) {
            if (option.isSinglePerson() || option.getImages().size() == 1) {
                searchResult = captureServiceHelper.parseResultOnePerson(resultSet, option, searchId);
            } else {
                searchResult = captureServiceHelper.parseResultNotOnePerson(resultSet, option, searchId);
            }
            //存储搜索历史记录
            SearchCollection collection = new SearchCollection();
            searchResult.setDeivceCount(retrunResult.getDeivceCount());
            collection.setSearchOption(option);
            collection.setSearchResult(searchResult);
            boolean flag = memoryDao.insertSearchRes(collection);
            if (memoryDao.getSearchRes(searchId).getSingleResults().size() > 0) {
                if (flag) {
                    log.info("The search history saved successful, search id is:" + searchId);
                } else {
                    log.warn("The search history saved failure, search id is:" + searchId);
                }
                retrunResult.setSearchId(searchResult.getSearchId());
                List<SingleSearchResult> singleSearchResults = new ArrayList<>();
                for (SingleSearchResult singleResult : searchResult.getSingleResults()) {
                    SingleSearchResult tempSingleResult = new SingleSearchResult();
                    tempSingleResult.setPictures(captureServiceHelper.pageSplit(singleResult.getPictures(),
                            option.getStart(),
                            option.getLimit()));
                    tempSingleResult.setSearchId(singleResult.getSearchId());
                    tempSingleResult.setTotal(singleResult.getTotal());
                    singleSearchResults.add(tempSingleResult);
                }
                retrunResult.setSingleResults(singleSearchResults);
            }
        } else {
            log.info("Start search picture, search result set is null");
        }
        sparkJDBCDao.closeConnection(searchCallBack.getConnection(), searchCallBack.getStatement());
        return retrunResult;
    }

    public SearchResult searchPicture2(SearchOption option, String searchId){
        String startDate = option.getStartTime().split(" ")[0];
        String endDate = option.getEndTime().split(" ")[0];
        List<Feature> features = new ArrayList<>();
        for(PictureData pictureData : option.getImages()){
            String id = pictureData.getImageID();
            byte[] bitFeature = pictureData.getFeature().getBitFeature();
            float[] feature = pictureData.getFeature().getFeature();
            features.add(new Feature(id, bitFeature, feature));
        }
        float sim = option.getSimilarity();
        boolean isTheSame = option.isSinglePerson();
        //组合参数
        CompareParam param = new CompareParam(startDate, endDate, features, sim, 20, isTheSame);
        param.setSort(option.getSort());
        List<String> ipcIds = new ArrayList<>();
        ipcIds.addAll(option.getIpcMapping().keySet());
        param.setIpcIds(ipcIds);
        SearchResult searchResult = client.compare(param, option, searchId);

        SearchResult retrunResult = new SearchResult();
        if (option.getDeviceIpcs() != null && option.getDeviceIpcs().size() > 0) {
            retrunResult.setDeivceCount(option.getDeviceIpcs().size());
        }

        //存储搜索历史记录
        SearchCollection collection = new SearchCollection();
        searchResult.setDeivceCount(retrunResult.getDeivceCount());
        collection.setSearchOption(option);
        collection.setSearchResult(searchResult);
        boolean flag = memoryDao.insertSearchRes(collection);
        if (memoryDao.getSearchRes(searchId).getSingleResults().size() > 0) {
            if (flag) {
                log.info("The search history saved successful, search id is:" + searchId);
            } else {
                log.warn("The search history saved failure, search id is:" + searchId);
            }
            retrunResult.setSearchId(searchResult.getSearchId());
            List<SingleSearchResult> singleSearchResults = new ArrayList<>();
            for (SingleSearchResult singleResult : searchResult.getSingleResults()) {
                SingleSearchResult tempSingleResult = new SingleSearchResult();
                tempSingleResult.setPictures(captureServiceHelper.pageSplit(singleResult.getPictures(),
                        option.getStart(),
                        option.getLimit()));
                tempSingleResult.setSearchId(singleResult.getSearchId());
                tempSingleResult.setTotal(singleResult.getTotal());
                singleSearchResults.add(tempSingleResult);
            }
            retrunResult.setSingleResults(singleSearchResults);
        }
        return retrunResult;
    }

    /**
     * 历史搜索记录查询
     *
     * @param resultOption 历史结果查询参数对象
     * @return SearchResult对象
     */
    public SearchResult getSearchResult(SearchResultOption resultOption) {
        SearchResult searchResult = null;
        SearchResult returnSearchResult = null;
        if (resultOption.getSearchId() != null && !"".equals(resultOption.getSearchId())) {
            searchResult = memoryDao.getSearchRes(resultOption.getSearchId());
            log.info("Start query searchResult, SearchResultOption is " + JacksonUtil.toJson(resultOption));
            if (searchResult != null) {
                if (resultOption.getSort() != null && resultOption.getSort().size() > 0) {
                    returnSearchResult = captureServiceHelper.sortByParamsAndPageSplit(searchResult, resultOption);
                    returnSearchResult.setDeivceCount(searchResult.getDeivceCount());
                    for (SingleSearchResult singleSearchResult : returnSearchResult.getSingleResults()) {
                        if (singleSearchResult.getDevicePictures() != null) {
                            for (GroupByIpc groupByIpc : singleSearchResult.getDevicePictures()) {
                                for (CapturedPicture capturedPicture : groupByIpc.getPictures()) {
                                    capturedPicture.setSabsolutepath(capturedPicture.getSabsolutepath());
                                    capturedPicture.setBabsolutepath(capturedPicture.getBabsolutepath());
                                }
                            }
                        } else {
                            for (CapturedPicture capturedPicture : singleSearchResult.getPictures()) {
                                capturedPicture.setSabsolutepath(capturedPicture.getSabsolutepath());
                                capturedPicture.setBabsolutepath(capturedPicture.getBabsolutepath());
                            }
                        }
                    }
                } else {
                    for (SingleSearchResult singleSearchResult : searchResult.getSingleResults()) {
                        captureServiceHelper.pageSplit(singleSearchResult.getPictures(), resultOption);
                    }
                    for (SingleSearchResult singleSearchResult : searchResult.getSingleResults()) {
                        for (CapturedPicture capturedPicture : singleSearchResult.getPictures()) {
                            capturedPicture.setSabsolutepath(capturedPicture.getSabsolutepath());
                            capturedPicture.setBabsolutepath(capturedPicture.getBabsolutepath());
                        }
                    }
                }
                if (resultOption.getSingleSearchResultOptions() != null
                        && resultOption.getSingleSearchResultOptions().size() > 0) {
                    List<SingleSearchResult> singleList = searchResult.getSingleResults();
                    List<SingleSearchResult> tempList = new ArrayList<>();
                    for (SingleSearchResult singleResult : singleList) {
                        boolean isContanis = false;
                        for (SingleResultOption singleResultOption : resultOption.getSingleSearchResultOptions()) {
                            if (Objects.equals((singleResult).getSearchId(), singleResultOption.getSearchId())) {
                                isContanis = true;
                            }
                        }
                        if (!isContanis) {
                            tempList.add(singleResult);
                        }
                    }
                    singleList.removeAll(tempList);
                }
            } else {
                log.error("Get query history failure, SearchResultOption is " + resultOption);
            }

        } else {
            log.info("SearchId is null");
        }
        return returnSearchResult;
    }

    /**
     * 查询设备最后一次抓拍时间
     *
     * @param ipcId 设备ID
     * @return 最后抓拍时间
     */
    public String getLastCaptureTime(String ipcId) {
        log.info("Start query last capture time, get ipcId is:" + ipcId);
        if (!StringUtils.isBlank(ipcId)) {
            String time = esDao.getLastCaptureTime(ipcId);
            log.info("Get query last capture time successful, time is:" + time);
            return time;
        }
        log.info("Get query last capture time failure, ipcId is null");
        return null;
    }

}
