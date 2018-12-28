package com.hzgc.cloud.dynrepo.dao;

import com.hzgc.common.collect.bean.FaceObject;
import com.hzgc.compare.CompareParam;
import com.hzgc.compare.Feature;
import com.hzgc.compare.client.CompareClient;
import com.hzgc.jniface.FaceUtil;
import com.hzgc.jniface.PictureData;
import com.hzgc.cloud.dynrepo.bean.CapturedPicture;
import com.hzgc.cloud.dynrepo.bean.SearchOption;
import com.hzgc.cloud.dynrepo.bean.SearchResult;
import com.hzgc.cloud.dynrepo.bean.SingleSearchResult;
import com.hzgc.cloud.dynrepo.service.CaptureServiceHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
@Slf4j
public class FaceCompareClient {

    @Autowired
    @SuppressWarnings("unused")
    private CaptureServiceHelper captureServiceHelper;
    @Autowired
    private ElasticSearchDao elasticSearchDao;


    public SearchResult compare(CompareParam param, SearchOption option, String searchId){
        boolean isTheSame = option.isSinglePerson();
        SearchResult searchResult = new SearchResult();
        List<SingleSearchResult> singleList = new ArrayList<>();
        searchResult.setSearchId(searchId);
        long start = System.currentTimeMillis();
        //单人单图检索
        if(option.getImages().size() == 1){
            com.hzgc.compare.SearchResult result = retrievalOnePerson(param);
            log.info("Time used to compare is :" + (System.currentTimeMillis() - start));
//            result = result.take(option.getStart(), option.getLimit());
            SingleSearchResult singleSearchResult = new SingleSearchResult();
            List<CapturedPicture> capturedPictures = captureServiceHelper.getCapturedPictures(result, option);
            singleSearchResult.setPictureDatas(option.getImages());
            singleSearchResult.setSearchId(searchId);
            singleSearchResult.setPictures(capturedPictures);
            singleSearchResult.setTotal(capturedPictures.size());
            singleList.add(singleSearchResult);
            //单人多图检索
        } else if(option.getImages().size() > 1 && isTheSame){
            com.hzgc.compare.SearchResult result = retrievalSamePerson(param);
            log.info("Time used to compare is :" + (System.currentTimeMillis() - start));
//            result = result.take(option.getStart(), option.getLimit());
            SingleSearchResult singleSearchResult = new SingleSearchResult();
            List<CapturedPicture> capturedPictures = captureServiceHelper.getCapturedPictures(result, option);
            singleSearchResult.setPictureDatas(option.getImages());
            singleSearchResult.setSearchId(searchId);
            singleSearchResult.setPictures(capturedPictures);
            singleSearchResult.setTotal(capturedPictures.size());
            singleList.add(singleSearchResult);
            //多人多图检索
        } else if (option.getImages().size() > 1 && !isTheSame){
            Map<String, com.hzgc.compare.SearchResult> map = retrievalNotSamePerson(param);
            log.info("Time used to compare is :" + (System.currentTimeMillis() - start));
            for(Map.Entry<String, com.hzgc.compare.SearchResult> entry : map.entrySet()){
                com.hzgc.compare.SearchResult searchResult1 = entry.getValue();
//                searchResult1 = searchResult1.take(option.getStart(), option.getLimit());
                SingleSearchResult singleSearchResult = new SingleSearchResult();
                List<CapturedPicture> capturedPictures = captureServiceHelper.getCapturedPictures(searchResult1, option);
                singleSearchResult.setPictures(capturedPictures);
                singleSearchResult.setTotal(capturedPictures.size());
                singleSearchResult.setSearchId(searchId);
                List<PictureData> pictureDatas = new ArrayList<>();
                for(PictureData pictureData : option.getImages()){
                    if(entry.getKey().equals(pictureData.getImageID())){
                        pictureDatas.add(pictureData);
                        break;
                    }
                }
                singleSearchResult.setPictureDatas(pictureDatas);
                singleList.add(singleSearchResult);
            }
        }
        searchResult.setSingleResults(singleList);
        return searchResult;
    }

    public com.hzgc.compare.SearchResult retrievalOnePerson(CompareParam param){
        byte[] bitFeature = param.getFeatures().get(0).getFeature1();
        float[] feature = param.getFeatures().get(0).getFeature2();
        String dateStart = param.getDateStart();
        String dateEnd = param.getDateEnd();
        //比特位特征值对比
        long start = System.currentTimeMillis();
        List<FaceObject> list = elasticSearchDao.searchByBitFeature(bitFeature, dateStart, dateEnd, param.getIpcIds(), 100);
        long firstCompared  = System.currentTimeMillis();
        log.info("The size of first compare result is " + list.size());
        log.info("The Time first compare used is " + (firstCompared - start));
        //float特征值对比
        com.hzgc.compare.SearchResult.Record[] records = new com.hzgc.compare.SearchResult.Record[list.size()];
        int index = 0;
        try {
            for (FaceObject faceObject : list) {
                float[] currentFeature = FaceUtil.base64Str2floatFeature(FaceUtil.floatFeature2Base64Str(feature));
                float[] historyFeature = faceObject.getAttribute().getFeature();
                float sim = FaceUtil.featureCompare(currentFeature, historyFeature);
                records[index] = new com.hzgc.compare.SearchResult.Record(sim, faceObject);
                index++;
            }
        }catch (Exception e){
            e.printStackTrace();
            log.error(e.getMessage());
        }
        long secondCompared  = System.currentTimeMillis();
        log.info("The size of second compare result is " + index);
        log.info("The Time second compare used is " + (secondCompared - firstCompared));
        com.hzgc.compare.SearchResult searchResult = new com.hzgc.compare.SearchResult(records);

        //排序返回
        List<Integer> sorts = param.getSort();
        if(sorts != null && (sorts.size() > 1 || (sorts.size() == 1 && sorts.get(0) != 4))){
            searchResult.sort(sorts);
        }
        searchResult.sortBySim();
        if(searchResult.getRecords()[0].getKey() < 100){
            log.warn("100% 图片不存在");
            log.info("BitFeature : " + FaceUtil.bitFeautre2Base64Str(bitFeature));
            log.info("Feature : " + FaceUtil.floatFeature2Base64Str(feature));
        }
        log.info("The size of sort result is " + searchResult.getRecords().length);
        log.info("The Time sort used is " + (System.currentTimeMillis() - secondCompared));
        return searchResult.take(param.getResultCount());
    }

    public com.hzgc.compare.SearchResult retrievalSamePerson(CompareParam param){
        //比特位特征值对比
        List<Feature> features = param.getFeatures();
        List<FaceObject> faceObjects = new ArrayList<>();
        HashSet<FaceObject> set = new HashSet<>();
        long start = System.currentTimeMillis();
        for(Feature featureObj : features){
            byte[] bitFeature = featureObj.getFeature1();
            String dateStart = param.getDateStart();
            String dateEnd = param.getDateEnd();
            List<FaceObject> list = elasticSearchDao.searchByBitFeature(bitFeature, dateStart, dateEnd, param.getIpcIds(), 100);
            set.addAll(list);
        }
        faceObjects.addAll(set);
        long firstCompared  = System.currentTimeMillis();
        log.info("The size of first compare result is " + faceObjects.size());
        log.info("The Time first compare used is " + (firstCompared - start));

        //float特征值对比
        com.hzgc.compare.SearchResult.Record[] records = new com.hzgc.compare.SearchResult.Record[faceObjects.size()];
        int index = 0;
        for(FaceObject faceObject : faceObjects){
            float[] historyFeature = faceObject.getAttribute().getFeature();
            float sim = 100.0f;
            for(Feature featureObj : features){
                float[] currentFeature = featureObj.getFeature2();
                float simTemp = FaceUtil.featureCompare(currentFeature, historyFeature);
                if(sim > simTemp){
                    sim = simTemp;
                }
            }
            records[index] = new com.hzgc.compare.SearchResult.Record(sim, faceObject);
            index ++;
        }
        com.hzgc.compare.SearchResult searchResult = new com.hzgc.compare.SearchResult(records);
        long secondCompared  = System.currentTimeMillis();
        log.info("The size of second compare result is " + index);
        log.info("The Time second compare used is " + (secondCompared - firstCompared));
        //排序返回
        List<Integer> sorts = param.getSort();
        if(sorts != null && (sorts.size() > 1 || (sorts.size() == 1 && sorts.get(0) != 4))){
            searchResult.sort(sorts);
        }
        searchResult.sortBySim();
        log.info("The size of sort result is " + searchResult.getRecords().length);
        log.info("The Time sort used is " + (System.currentTimeMillis() - secondCompared));
        return searchResult.take(param.getResultCount());
    }

    public Map<String, com.hzgc.compare.SearchResult> retrievalNotSamePerson(CompareParam param){
        //TODO 多人多图检索
        return new HashMap<>();
    }
}
