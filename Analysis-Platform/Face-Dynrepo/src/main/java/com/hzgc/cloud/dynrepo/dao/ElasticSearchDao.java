package com.hzgc.cloud.dynrepo.dao;

import com.hzgc.common.collect.bean.FaceObject;
import com.hzgc.common.util.es.ElasticSearchHelper;
import com.hzgc.common.service.faceattribute.bean.Attribute;
import com.hzgc.common.service.faceattribute.bean.AttributeValue;
import com.hzgc.common.service.facedynrepo.FaceTable;
import com.hzgc.cloud.dynrepo.bean.CaptureOption;
import com.hzgc.jniface.FaceAttribute;
import com.hzgc.jniface.FaceUtil;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.lucene.search.function.CombineFunction;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermsQueryBuilder;
import org.elasticsearch.index.query.functionscore.FunctionScoreQueryBuilder;
import org.elasticsearch.index.query.functionscore.ScoreFunctionBuilders;
import org.elasticsearch.index.query.functionscore.ScriptScoreFunctionBuilder;
import org.elasticsearch.script.Script;
import org.elasticsearch.script.ScriptType;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.util.*;

import static org.elasticsearch.common.lucene.search.function.FiltersFunctionScoreQuery.ScoreMode.FIRST;

@Repository
public class ElasticSearchDao {
    private TransportClient esClient;


    public ElasticSearchDao(@Value("${es.cluster.name}") String clusterName,
                            @Value("${es.hosts}") String esHost,
                            @Value("${es.cluster.port}") String esPort) {
        this.esClient = ElasticSearchHelper.getEsClient(clusterName, esHost, Integer.parseInt(esPort));
    }


    public SearchResponse getCaptureHistory(CaptureOption option, String sortParam) {
        BoolQueryBuilder queryBuilder = createBoolQueryBuilder(option);

        SearchRequestBuilder requestBuilder = createSearchRequestBuilder()
                .setQuery(queryBuilder)
                .setFrom(option.getStart())
                .setSize(option.getLimit())
                .addSort(FaceTable.TIMESTAMP,
                        Objects.equals(sortParam, EsSearchParam.DESC) ? SortOrder.DESC : SortOrder.ASC);
        return requestBuilder.get();
    }

    public SearchResponse getCaptureHistory(CaptureOption option, List<String> ipcList, String sortParam) {
        BoolQueryBuilder queryBuilder = createBoolQueryBuilder(option);
        setDeviceIdList(queryBuilder, ipcList);
        SearchRequestBuilder requestBuilder = createSearchRequestBuilder()
                .setQuery(queryBuilder)
                .setFrom(option.getStart())
                .setSize(option.getLimit())
                .addSort(FaceTable.TIMESTAMP,
                        Objects.equals(sortParam, EsSearchParam.DESC) ? SortOrder.DESC : SortOrder.ASC);
        return requestBuilder.get();
    }

    public SearchResponse getCaptureHistory(CaptureOption option, String ipc, String sortParam) {
        BoolQueryBuilder queryBuilder = createBoolQueryBuilder(option);
        setDeviceId(queryBuilder, ipc);
        SearchRequestBuilder requestBuilder = createSearchRequestBuilder()
                .setQuery(queryBuilder)
                .setFrom(option.getStart())
                .setSize(option.getLimit())
                .addSort(FaceTable.TIMESTAMP,
                        Objects.equals(sortParam, EsSearchParam.DESC) ? SortOrder.DESC : SortOrder.ASC);
        return requestBuilder.get();
    }

    private SearchRequestBuilder createSearchRequestBuilder() {
        return esClient.prepareSearch(FaceTable.DYNAMIC_INDEX)
                .setTypes(FaceTable.PERSON_INDEX_TYPE);
    }

    private BoolQueryBuilder createBoolQueryBuilder(CaptureOption option) {
        // 最终封装成的boolQueryBuilder 对象。
        BoolQueryBuilder totalBQ = QueryBuilders.boolQuery();
        //筛选人脸属性
        if (option.getAttributes() != null && option.getAttributes().size() > 0) {
            setAttribute(totalBQ, option.getAttributes());
        }
//        totalBQ.must(queryBuilder);
        // 开始时间和结束时间存在的时候的处理
        if (option.getStartTime() != null && option.getEndTime() != null &&
                !option.getStartTime().equals("") && !option.getEndTime().equals("")) {
            setStartEndTime(totalBQ, option.getStartTime(), option.getEndTime());
        }
        return totalBQ;
    }

    private void setStartEndTime(BoolQueryBuilder totalBQ, String startTime, String endTime) {
        totalBQ.must(QueryBuilders.rangeQuery(FaceTable.TIMESTAMP).gte(startTime).lte(endTime));
    }

    private void setDeviceIdList(BoolQueryBuilder totalBQ, List<String> deviceId) {
        // 设备ID 的的boolQueryBuilder
        BoolQueryBuilder devicdIdBQ = QueryBuilders.boolQuery();
        for (String t : deviceId) {
            devicdIdBQ.should(QueryBuilders.matchPhraseQuery(FaceTable.IPCID, t));
        }
        totalBQ.must(devicdIdBQ);
    }

    private void setDeviceId(BoolQueryBuilder totalBQ, String ipc) {
        BoolQueryBuilder deviceIdBQ = QueryBuilders.boolQuery();
        deviceIdBQ.should(QueryBuilders.matchPhraseQuery(FaceTable.IPCID, ipc).analyzer(EsSearchParam.STANDARD));
        totalBQ.must(deviceIdBQ);
    }

    private void setAttribute(BoolQueryBuilder totalBQ, List <Attribute> attributes) {
        // 筛选行人属性
//        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        for (Attribute attribute : attributes) {
            String identify = attribute.getIdentify();
            List <AttributeValue> attributeValues = attribute.getValues();
            BoolQueryBuilder attributeBuilder = QueryBuilders.boolQuery();
            for (AttributeValue attributeValue : attributeValues) {
                Integer attr = attributeValue.getValue();
                attributeBuilder.should(QueryBuilders.matchQuery(identify, attr));
            }
            totalBQ.must(attributeBuilder);
        }
    }

    public String getLastCaptureTime(String ipcId) {
        BoolQueryBuilder totalBQ = QueryBuilders.boolQuery();
        totalBQ.must(QueryBuilders.matchPhraseQuery(FaceTable.IPCID, ipcId));
        SearchResponse searchResponse = createSearchRequestBuilder()
                .setQuery(totalBQ)
                .setSize(1)
                .addSort(FaceTable.TIMESTAMP, SortOrder.DESC)
                .get();
        SearchHits hits =  searchResponse.getHits();
        SearchHit[] searchHits = hits.getHits();
        String lastTime = "";
        for (SearchHit hit : searchHits){
            lastTime = String.valueOf(hit.getSource().get(FaceTable.TIMESTAMP));
        }
        return lastTime;
    }

    public List<FaceObject> searchByBitFeature(byte[] bitFeature, String dateStart, String dateEnd, List<String> ipcs, int size){
        SearchRequestBuilder requestBuilder = createSearchRequestBuilder();
        Map<String,Object> params = new HashMap<>();
        params.put("bit",true);
        params.put("field", FaceTable.BITFEATURE);
        params.put("vector", FaceUtil.bitFeautre2Base64Str(bitFeature));

        QueryBuilder queryBuilder = QueryBuilders.rangeQuery(FaceTable.TIMESTAMP).gte(dateStart).lte(dateEnd);
        ScriptScoreFunctionBuilder scriptScoreFunctionBuilder = ScoreFunctionBuilders.scriptFunction(
                new Script(ScriptType.INLINE, "knn", "binary_vector_score", params)
        );
        FunctionScoreQueryBuilder query = QueryBuilders.functionScoreQuery(queryBuilder, scriptScoreFunctionBuilder)
                .scoreMode(FIRST).boostMode(CombineFunction.REPLACE);
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery().must(query);
        if(ipcs != null && ipcs.size() != 0){
//            log.info("IpcIds size : " + ipcIds.size());
//            log.info("IpcIds : " + ipcIds.get(0) + " ...");
            TermsQueryBuilder queryBuilder2 = QueryBuilders.termsQuery("ipcid", ipcs.toArray(new String[ipcs.size()]));
            boolQueryBuilder.must(queryBuilder2);
        }
        requestBuilder.setQuery(boolQueryBuilder);
        requestBuilder.setSize(size);
        SearchResponse response = requestBuilder.execute().actionGet();
        SearchHit[] hits = response.getHits().getHits();
        return analyseResult(hits);
    }

    private List<FaceObject> analyseResult(SearchHit[] hits) {
        List<FaceObject> res = new ArrayList<>();
        for(SearchHit hit : hits){
            Map<String, Object> source = hit.getSource();
            FaceObject faceObject = new FaceObject();
            faceObject.setId(hit.getId());
            FaceAttribute attribute = new FaceAttribute();
            // TODO
            attribute.setBitFeature(FaceUtil.base64Str2BitFeature((String)hit.getSource().get(FaceTable.BITFEATURE)));
            attribute.setFeature(FaceUtil.base64Str2floatFeature((String) hit.getSource().get(FaceTable.FEATURE)));
            attribute.setAge((Integer) hit.getSource().get(FaceTable.AGE));
            attribute.setEyeglasses((Integer) hit.getSource().get(FaceTable.EYEGLASSES));
            attribute.setGender((Integer) hit.getSource().get(FaceTable.GENDER));
            attribute.setHuzi((Integer) hit.getSource().get(FaceTable.HUZI));
            attribute.setMask((Integer) hit.getSource().get(FaceTable.MASK));
            faceObject.setAttribute(attribute);
            faceObject.setHostname((String) source.get(FaceTable.HOSTNAME));
            faceObject.setTimeStamp((String) source.get(FaceTable.TIMESTAMP));
            faceObject.setIpcId((String) source.get(FaceTable.IPCID));
            faceObject.setsAbsolutePath((String) source.get(FaceTable.SABSOLUTEPATH));
            faceObject.setbAbsolutePath((String) source.get(FaceTable.BABSOLUTEPATH));
            res.add(faceObject);
        }
        System.out.println("The size of response is " + res.size());
        return res;
    }
}
