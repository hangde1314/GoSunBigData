package com.hzgc.service.dynrepo.service;

import com.hzgc.common.util.json.JacksonUtil;
import com.hzgc.service.dynrepo.bean.*;
import com.hzgc.service.dynrepo.dao.ElasticSearchDao;
import com.hzgc.service.dynrepo.dao.HBaseDao;
import com.hzgc.service.dynrepo.dao.SparkJDBCDao;
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
    private HBaseDao hBaseDao;
    @Autowired
    @SuppressWarnings("unused")
    private ElasticSearchDao esDao;
    @Autowired
    @SuppressWarnings("unused")
    private CaptureServiceHelper captureServiceHelper;

    public SearchResult searchPicture(SearchOption option, String searchId) throws SQLException {
        SearchResult searchResult = null;
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
            collection.setSearchOption(option);
            collection.setSearchResult(searchResult);
            boolean flag = hBaseDao.insertSearchRes(collection);
            if (searchResult.getSingleResults().size() > 0) {
                if (flag) {
                    log.info("The search history saved successful, search id is:" + searchId);
                } else {
                    log.warn("The search history saved failure, search id is:" + searchId);
                }
                for (SingleSearchResult singleResult : searchResult.getSingleResults()) {
                    singleResult.setPictures(captureServiceHelper.pageSplit(singleResult.getPictures(),
                            option.getStart(),
                            option.getLimit()));
                }
            }
        } else {
            log.info("Start search picture, search result set is null");
        }
        sparkJDBCDao.closeConnection(searchCallBack.getConnection(), searchCallBack.getStatement());
        return searchResult;
    }

    /**
     * 获取搜索原图
     *
     * @param image_name 原图ID
     * @return 图片二进制
     */
    public byte[] getSearchPicture(String image_name) {
        if (!StringUtils.isBlank(image_name)) {
            return hBaseDao.getSearchPicture(image_name);
        }
        return null;
    }

    /**
     * 查询搜索记录
     *
     * @param start_time 历史记录起始时间
     * @param end_time   历史记录结束时间
     * @param sort       排序参数
     * @param start      起始位置
     * @param limit      返回条数
     * @return 返回查询结果
     */
    public List<SearchHisotry> getSearchHistory(String start_time, String end_time, String sort, int start, int limit) {
        return hBaseDao.getSearchHistory(start_time, end_time, sort, start, limit);
    }

    /**
     * 历史搜索记录查询
     *
     * @param resultOption 历史结果查询参数对象
     * @return SearchResult对象
     */
    public SearchResult getSearchResult(SearchResultOption resultOption) {
        SearchResult searchResult = null;
        if (resultOption.getSearchId() != null && !"".equals(resultOption.getSearchId())) {
            searchResult = hBaseDao.getSearchRes(resultOption.getSearchId());
            log.info("Start query searchResult, SearchResultOption is " + JacksonUtil.toJson(resultOption));
            if (searchResult != null) {
                if (resultOption.getSort() != null && resultOption.getSort().size() > 0) {
                    captureServiceHelper.sortByParamsAndPageSplit(searchResult, resultOption);
                    for (SingleSearchResult singleSearchResult : searchResult.getSingleResults()) {
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
        return searchResult;
    }

    /**
     * 查询设备最后一次抓拍时间
     *
     * @param deviceId 设备ID
     * @return 最后抓拍时间
     */
    public String getLastCaptureTime(String deviceId) {
        String ipcId = captureServiceHelper.deviceIdToIpcId(deviceId);
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
