package com.hzgc.compare.worker.persistence;

import com.hzgc.common.collect.bean.FaceObject;
import com.hzgc.common.service.facedynrepo.FaceTable;
import com.hzgc.common.util.es.ElasticSearchHelper;
import com.hzgc.compare.worker.conf.Config;
import com.hzgc.jniface.FaceAttribute;
import com.hzgc.jniface.FaceUtil;
import javafx.util.Pair;
import org.apache.log4j.Logger;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermsQueryBuilder;
import org.elasticsearch.search.SearchHit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
/**
 * 负责从elasticsearch读取数据
 */

public class ElasticSearchClient {
    //    private static final Logger logger = LoggerFactory.getLogger(ElasticSearchClient.class);
    private static Logger log = Logger.getLogger(ElasticSearchClient.class);
    private static TransportClient esClient;

    public static void connect(){
        esClient = ElasticSearchHelper.getEsClient(Config.ES_CLUSTER_NAME, Config.ES_HOST, Config.ES_CLUSTER_PORT);
    }
    /**
     * 根据第一次比较的结果，查询EsHBas中的数据
     * @param ids 第一次对比的结果，id集合
     * @return 返回结果集
     */
    public static List<FaceObject> readFromEs(List<String> ids, List<String> ipcIds){
        log.info("The size of ids is " + ids.size());
        SearchRequestBuilder requestBuilder = esClient.prepareSearch(FaceTable.DYNAMIC_INDEX)
                .setTypes(FaceTable.PERSON_INDEX_TYPE);
//        requestBuilder.addStoredField("");
        requestBuilder.setSize(500);
        log.info("Ids : " + ids.toString());

        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        if(ipcIds != null && ipcIds.size() > 0) {
            log.info("IpcIds : " + ipcIds.toString());
            TermsQueryBuilder queryBuilder2 = QueryBuilders.termsQuery("ipcid", ipcIds.toArray(new String[ipcIds.size()]));
            boolQueryBuilder.must(queryBuilder2);
        }
        TermsQueryBuilder queryBuilder = QueryBuilders.termsQuery("_id", ids.toArray(new String[ids.size()]));
        boolQueryBuilder.must(queryBuilder);
        requestBuilder.setQuery(boolQueryBuilder);
        long start = System.currentTimeMillis();
        SearchResponse response = requestBuilder.execute().actionGet();
        log.info("The time used to get data from ES is : " + (System.currentTimeMillis() - start));
        SearchHit[] hits = response.getHits().getHits();
        return analyseResult(hits);
    }
    /**
     * 根据过滤结果，查询Es中的数据
     * @param records 过滤后的内存数据
     * @return 返回结果集
     */
    public static List<FaceObject> readFromEs2(List<Pair<String, byte[]>> records, List<String> ipcIds){
        log.info("The size of records is " + records.size());
        String[] ids = new String[records.size()];
        int index = 0;
        for(Pair<String, byte[]> record : records){
            ids[index] = record.getKey();
            index ++;
        }
        SearchRequestBuilder requestBuilder = esClient.prepareSearch(FaceTable.DYNAMIC_INDEX)
                .setTypes(FaceTable.PERSON_INDEX_TYPE);
        requestBuilder.setSize(500);
        log.info("Ids : " + Arrays.toString(ids));

        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        if(ipcIds != null && ipcIds.size() > 0) {
            log.info("IpcIds : " + ipcIds.toString());
            TermsQueryBuilder queryBuilder2 = QueryBuilders.termsQuery("ipcid", ipcIds.toArray(new String[ipcIds.size()]));
            boolQueryBuilder.must(queryBuilder2);
        }
        TermsQueryBuilder queryBuilder = QueryBuilders.termsQuery("_id", ids);
        boolQueryBuilder.must(queryBuilder);
        requestBuilder.setQuery(boolQueryBuilder);
        long start = System.currentTimeMillis();
        SearchResponse response = requestBuilder.execute().actionGet();
        log.info("The time used to get data from ES is : " + (System.currentTimeMillis() - start));
        SearchHit[] hits = response.getHits().getHits();
        return analyseResult(hits);
    }

    public static void main(String args[]){
        ElasticSearchClient.connect();
        List<String> ids = new ArrayList<>();
        ids.add("b4372312b1a64149b3d03ef55d3a1d77");
        ids.add("db518ad9dab14693be806d1dcab5197e");
        List<String> ipcIds = new ArrayList<>();
        ipcIds.add("4H05C95PAA33515");
        List<FaceObject> list = ElasticSearchClient.readFromEs(ids, ipcIds);
        for(FaceObject obj : list){
            System.out.println("FaceObject Id : " + obj.getId());
            System.out.println("FaceObject : " + obj);
        }
    }

    private static List<FaceObject> analyseResult(SearchHit[] hits){
        List<FaceObject> res = new ArrayList<>();
        for(SearchHit hit : hits){
            FaceObject faceObject = new FaceObject();
            faceObject.setId(hit.getId());
            faceObject.setIpcId((String) hit.getSource().get(FaceTable.IPCID));
            faceObject.setTimeStamp((String) hit.getSource().get(FaceTable.TIMESTAMP));
            faceObject.setsFtpUrl((String) hit.getSource().get(FaceTable.SFTPURL));
            faceObject.setbFtpUrl((String) hit.getSource().get(FaceTable.BFTPURL));
            faceObject.setsAbsolutePath((String) hit.getSource().get(FaceTable.SABSOLUTEPATH));
            faceObject.setbAbsolutePath((String) hit.getSource().get(FaceTable.BABSOLUTEPATH));
            faceObject.setHostname((String) hit.getSource().get(FaceTable.HOSTNAME));
            FaceAttribute attribute = new FaceAttribute();
            attribute.setFeature(FaceUtil.base64Str2floatFeature((String) hit.getSource().get(FaceTable.FEATURE)));
            attribute.setBitFeature(FaceUtil.base64Str2BitFeature((String) hit.getSource().get(FaceTable.BITFEATURE)));
            attribute.setAge((Integer) hit.getSource().get(FaceTable.AGE));
            attribute.setEyeglasses((Integer) hit.getSource().get(FaceTable.EYEGLASSES));
            attribute.setGender((Integer) hit.getSource().get(FaceTable.GENDER));
            attribute.setHuzi((Integer) hit.getSource().get(FaceTable.HUZI));
            attribute.setMask((Integer) hit.getSource().get(FaceTable.MASK));
            attribute.setSharpness((Integer) hit.getSource().get(FaceTable.SHARPNESS));
            faceObject.setAttribute(attribute);
            res.add(faceObject);
        }
        return res;
    }
}