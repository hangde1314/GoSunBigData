package com.hzgc.compare.worker.compare;

import com.hzgc.common.collect.bean.FaceObject;
import com.hzgc.compare.Feature;
import com.hzgc.compare.SearchResult;
import com.hzgc.compare.worker.memory.cache.MemoryCacheImpl;
import com.hzgc.jniface.CompareResult;
import com.hzgc.jniface.FaceFeatureInfo;
import com.hzgc.jniface.FaceJNI;
import javafx.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class ComparatorsImpl implements Comparators{
    private static final Logger logger = LoggerFactory.getLogger(ComparatorsImpl.class);

    public List<Pair<String, byte[]>> filter(String dateStart, String dateEnd) {
        List<Pair<String, byte[]>> result = new ArrayList<>();
        Map<String, List<Pair<String, byte[]>>> cacheRecords =
                MemoryCacheImpl.getInstance().getCacheRecords();
//        Set<Triplet<String, String, String>> temp = new HashSet<>();
//        temp.addAll(cacheRecords.keySet());
        Iterator<String> iterator =  cacheRecords.keySet().iterator();
        Long start = System.currentTimeMillis();
        while (iterator.hasNext()) {
            String key = iterator.next();
            if (key.compareTo(dateStart) >= 0 &&
                    key.compareTo(dateEnd) <= 0) {
                result.addAll(cacheRecords.get(key));
            }
        }
        logger.info("The time used to filter is : " + (System.currentTimeMillis() - start));
        logger.info("The size of filtere result is : " + result.size());
        return result;
    }

    @Override
    public List<String> compareFirst(byte[] feature, int num, List<Pair<String, byte[]>> data) {
        Long start = System.currentTimeMillis();
//        FeatureCompared.compareFirst(data, feature, num);
        List<String> rowkeys = new ArrayList<>();
        byte[][] diku = new byte[data.size()][32];
        int index = 0;
        for(Pair<String, byte[]> pair : data){
            diku[index] = pair.getValue();
            index ++;
        }

        byte[][] queryList = new byte[1][32];
        queryList[0] = feature;
        logger.info("The time insert dataes when first compare used is : " + (System.currentTimeMillis() - start));
        ArrayList<CompareResult> array = FaceJNI.faceCompareBit(diku, queryList, num);
        for(FaceFeatureInfo featureInfo : array.get(0).getPictureInfoArrayList()){
            String rowkey = data.get(featureInfo.getIndex()).getKey();
            rowkeys.add(rowkey);
        }
        logger.info("The time first compare used is : " + (System.currentTimeMillis() - start));
        return rowkeys;
    }

    public List<String> compareFirstNotSamePerson(List<Feature> features, int num, List<Pair<String, byte[]>> data){
        Long start = System.currentTimeMillis();
        byte[][] diku = new byte[data.size()][32];
        int index = 0;
        for(Pair<String, byte[]> pair : data){
            diku[index] = pair.getValue();
            index ++;
        }

        byte[][] queryList = new byte[features.size()][32];
        index = 0;
        for (Feature feature : features){
            queryList[index] = feature.getFeature1();
            index ++;
        }
        logger.info("The time insert dataes when first compare used is : " + (System.currentTimeMillis() - start));
        FaceJNI.init();
        ArrayList<CompareResult> array = FaceJNI.faceCompareBit(diku, queryList, num);
        List<String> rowkeys = new ArrayList<>();
        for(CompareResult compareResult : array){
            for(FaceFeatureInfo faceFeatureInfo : compareResult.getPictureInfoArrayList()){
                String rowkey = data.get(faceFeatureInfo.getIndex()).getKey();
                if(!rowkeys.contains(rowkey)) {
                    rowkeys.add(rowkey);
                }
            }
        }
        logger.info("The time first compare used is : " + (System.currentTimeMillis() - start));
        return rowkeys;
    }

    @Override
    public List<String> compareFirstTheSamePerson(List<byte[]> features, int num, List<Pair<String, byte[]>> data) {
        Long start = System.currentTimeMillis();
        List<String> result = new ArrayList<>();
        byte[][] diku = new byte[data.size()][32];
        int index = 0;
        for(Pair<String, byte[]> pair : data){
            diku[index] = pair.getValue();
            index ++;
        }

        byte[][] queryList = new byte[features.size()][32];
        index = 0;
        for (byte[] feature : features){
            queryList[index] = feature;
            index ++;
        }
        Set<String> set = new HashSet<>();
        index = 0;
        logger.info("The time insert dataes when first compare used is : " + (System.currentTimeMillis() - start));
        ArrayList<CompareResult> array = FaceJNI.faceCompareBit(diku, queryList, num * 3);
        for(CompareResult compareResult : array){
            List<String> rowkeys = new ArrayList<>();
            for(FaceFeatureInfo faceFeatureInfo : compareResult.getPictureInfoArrayList()){
                String rowkey = data.get(faceFeatureInfo.getIndex()).getKey();
                rowkeys.add(rowkey);
            }
            if(index == 0) {
                set.addAll(rowkeys);
            }else {
                set.retainAll(rowkeys);
            }
            index ++;
        }
        result.addAll(set);
        logger.info("The time first compare used is : " + (System.currentTimeMillis() - start));
        return result;
    }

    @Override
    public SearchResult compareSecond(float[] feature, float sim, List<FaceObject> datas, List<Integer> sorts) {
        Long start = System.currentTimeMillis();
        float[][] diku = new float[datas.size()][512];
        int index = 0;
        for(FaceObject faceobj : datas){
            diku[index] = faceobj.getAttribute().getFeature();
            index ++;
        }

        float[][] queryList = new float[1][512];
        queryList[0] = feature;

        ArrayList<CompareResult> array = FaceJNI.faceCompareFloat(diku, queryList, 30);
        if(array == null || array.size() == 0){
            return new SearchResult();
        }
        List<FaceFeatureInfo> list = array.get(0).getPictureInfoArrayList();
        SearchResult.Record[] records = new SearchResult.Record[list.size()];
        index = 0;
        for(FaceFeatureInfo faceFeatureInfo : list){
            float score = faceFeatureInfo.getScore();
            if(score < sim){
                continue;
            }
            FaceObject body = datas.get(faceFeatureInfo.getIndex());
            SearchResult.Record record = new SearchResult.Record(score, body);
            records[index] = record;
            index ++;
        }
        SearchResult result = new SearchResult(records);
        long compared = System.currentTimeMillis();
        logger.info("The time second compare used is : " + (compared - start));
        if(sorts != null && (sorts.size() > 1 || (sorts.size() == 1 && sorts.get(0) != 4))){
            result.sort(sorts);
        } else {
            result.sortBySim();
        }
        logger.info("The time used to sort is : " + (System.currentTimeMillis() - compared));
        return result;
    }

    @Override
    public SearchResult compareSecondTheSamePerson(List<float[]> features, float sim, List<FaceObject> datas, List<Integer> sorts) {
        Long start = System.currentTimeMillis();
        float[][] diku = new float[datas.size()][512];
        int index = 0;
        for(FaceObject faceobj : datas){
            diku[index] = faceobj.getAttribute().getFeature();
            index ++;
        }

        float[][] queryList = new float[1][512];
        index = 0;
        for(float[] feature : features){
            queryList[index] = feature;
            index ++;
        }
        ArrayList<CompareResult> array = FaceJNI.faceCompareFloat(diku, queryList, 30);
        if(array == null || array.size() == 0){
            return new SearchResult();
        }
        Map<FaceObject, Float> temp = new HashMap<>();
        for(CompareResult compareResult : array){
            ArrayList<FaceFeatureInfo> faceFeatureInfos = compareResult.getPictureInfoArrayList();
            for(FaceFeatureInfo faceFeatureInfo : faceFeatureInfos){
                FaceObject face = datas.get(faceFeatureInfo.getIndex());
                float score = faceFeatureInfo.getScore();
                if(score < sim){
                    continue;
                }
                float scoreTemp = temp.get(face);
                if(score < scoreTemp){
                    temp.put(face, score);
                }
            }
        }
        Set<Map.Entry<FaceObject, Float>> entrySet = temp.entrySet();
        SearchResult.Record[] records = new SearchResult.Record[entrySet.size()];
        index = 0;
        for(Map.Entry<FaceObject, Float> entry : entrySet){
            records[index] = new SearchResult.Record(entry.getValue(), entry.getKey());
            index ++;
        }
        SearchResult result = new SearchResult(records);
        long compared = System.currentTimeMillis();
        logger.info("The time second compare used is : " + (compared - start));
        if(sorts != null && (sorts.size() > 1 || (sorts.size() == 1 && sorts.get(0) != 4))) {
            result.sort(sorts);
        } else {
            result.sortBySim();
        }
        logger.info("The time used to sort is : " + (System.currentTimeMillis() - compared));
        return result;
    }

    @Override
    public Map<String, SearchResult> compareSecondNotSamePerson(List<Feature> features, float sim,
                                                                List<FaceObject> datas, List<Integer> sorts) {
        Long start = System.currentTimeMillis();
        Map<String, SearchResult> result = new HashMap<>();
        float[][] diku = new float[datas.size()][512];
        int index = 0;
        for(FaceObject faceobj : datas){
            diku[index] = faceobj.getAttribute().getFeature();
            index ++;
        }

        float[][] queryList = new float[features.size()][512];
        index = 0;
        for(Feature feature : features){
            queryList[index] = feature.getFeature2();
            index ++;
        }
        ArrayList<CompareResult> array = FaceJNI.faceCompareFloat(diku, queryList, 30);
        if(array == null || array.size() == 0){
            return new HashMap<>();
        }
        for(CompareResult compareResult : array){
            SearchResult.Record[] records = new SearchResult.Record[compareResult.getPictureInfoArrayList().size()];
            int recordIndex = 0;
            for(FaceFeatureInfo faceFeatureInfo : compareResult.getPictureInfoArrayList()){
                float score = faceFeatureInfo.getScore();
                if(score < sim){
                    continue;
                }
                FaceObject faceObject = datas.get(faceFeatureInfo.getIndex());
                records[recordIndex] = new SearchResult.Record(score, faceObject);
                recordIndex ++;
            }
            SearchResult searchResult = new SearchResult(records);
            if(sorts != null && (sorts.size() > 1 || (sorts.size() == 1 && sorts.get(0) != 4))) {
                searchResult.sort(sorts);
            } else {
                searchResult.sortBySim();
            }
            String id = features.get(Integer.parseInt(compareResult.getIndex())).getId();
            result.put(id, searchResult);
        }
        logger.info("The time second compare used is : " + (System.currentTimeMillis() - start));
        return result;
    }
}
