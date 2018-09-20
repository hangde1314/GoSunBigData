package com.hzgc.compare.worker.persistence;

import com.hzgc.common.collect.bean.FaceObject;
import com.hzgc.common.service.facedynrepo.FaceTable;
import com.hzgc.common.util.es.ElasticSearchHelper;
import com.hzgc.compare.worker.conf.Config;
import com.hzgc.jniface.FaceAttribute;
import com.hzgc.jniface.FaceFunction;
import javafx.util.Pair;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermsQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
 import java.util.ArrayList;
import java.util.List;
 /**
 * 负责从elasticsearch读取数据
 */
public class ElasticSearchClient {
    private static final Logger logger = LoggerFactory.getLogger(ElasticSearchClient.class);
     private TransportClient esClient;
     public ElasticSearchClient() {
        this.esClient = ElasticSearchHelper.getEsClient(Config.ES_CLUSTER_NAME, Config.ES_HOST, Config.ES_CLUSTER_PORT);
    }
     /**
     * 根据第一次比较的结果，查询EsHBas中的数据
     * @param ids 第一次对比的结果，id集合
     * @return 返回结果集
     */
    public List<FaceObject> readFromEs(List<String> ids){
        logger.info("The size of ids is " + ids.size());
        SearchRequestBuilder requestBuilder = esClient.prepareSearch(FaceTable.DYNAMIC_INDEX)
                .setTypes(FaceTable.PERSON_INDEX_TYPE);
        requestBuilder.addStoredField("");
        requestBuilder.setSize(500);
        TermsQueryBuilder queryBuilder = QueryBuilders.termsQuery("id", ids.toArray(new String[ids.size()]));
        requestBuilder.setQuery(queryBuilder);
        long start = System.currentTimeMillis();
        SearchResponse response = requestBuilder.execute().actionGet();
        logger.info("The time used to get data from ES is : " + (System.currentTimeMillis() - start));
        SearchHit[] hits = response.getHits().getHits();
        return analyseResult(hits);
    }
     /**
     * 根据过滤结果，查询Es中的数据
     * @param records 过滤后的内存数据
     * @return 返回结果集
     */
    public List<FaceObject> readFromEs2(List<Pair<String, byte[]>> records){
        logger.info("The size of records is " + records.size());
        String[] ids = new String[records.size()];
        int index = 0;
        for(Pair<String, byte[]> record : records){
            ids[index] = record.getKey();
        }
        SearchRequestBuilder requestBuilder = esClient.prepareSearch(FaceTable.DYNAMIC_INDEX)
                .setTypes(FaceTable.PERSON_INDEX_TYPE);
        requestBuilder.addStoredField("");
        requestBuilder.setSize(500);
        TermsQueryBuilder queryBuilder = QueryBuilders.termsQuery("id", ids);
        requestBuilder.setQuery(queryBuilder);
        long start = System.currentTimeMillis();
        SearchResponse response = requestBuilder.execute().actionGet();
        logger.info("The time used to get data from ES is : " + (System.currentTimeMillis() - start));
        SearchHit[] hits = response.getHits().getHits();
        return analyseResult(hits);
    }
     private List<FaceObject> analyseResult(SearchHit[] hits){
        List<FaceObject> res = new ArrayList<>();
        for(SearchHit hit : hits){
            FaceObject faceObject = new FaceObject();
            faceObject.setId(hit.getId());
            faceObject.setIpcId(hit.getField(FaceTable.IPCID).getValue());
            faceObject.setTimeStamp(hit.getField(FaceTable.TIMESTAMP).getValue());
            faceObject.setsFtpUrl(hit.getField(FaceTable.SFTPURL).getValue());
            faceObject.setbFtpUrl(hit.getField(FaceTable.BFTPURL).getValue());
            faceObject.setsAbsolutePath(hit.getField(FaceTable.SABSOLUTEPATH).getValue());
            faceObject.setbAbsolutePath(hit.getField(FaceTable.BABSOLUTEPATH).getValue());
            faceObject.setHostname(hit.getField(FaceTable.HOSTNAME).getValue());
            // TODO 缺IP
//            faceObject.setIp(hit.getField(FaceTable.))
            FaceAttribute attribute = new FaceAttribute();
            attribute.setFeature(FaceFunction.base64Str2floatFeature(hit.getField(FaceTable.FEATURE).getValue()));
            attribute.setBitFeature(FaceFunction.base64Str2BitFeature(hit.getField(FaceTable.BITFEATURE).getValue()));
            attribute.setAge(hit.getField(FaceTable.AGE).getValue());
            attribute.setEyeglasses(hit.getField(FaceTable.EYEGLASSES).getValue());
            attribute.setGender(hit.getField(FaceTable.GENDER).getValue());
            attribute.setHuzi(hit.getField(FaceTable.HUZI).getValue());
            attribute.setMask(hit.getField(FaceTable.MASK).getValue());
            faceObject.setAttribute(attribute);
            res.add(faceObject);
        }
        return res;
    }
}