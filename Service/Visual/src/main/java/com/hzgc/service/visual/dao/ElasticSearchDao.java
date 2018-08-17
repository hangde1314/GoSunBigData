package com.hzgc.service.visual.dao;

import com.hzgc.common.es.ElasticSearchHelper;
import com.hzgc.common.table.dynrepo.DynamicTable;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.text.SimpleDateFormat;
import java.util.List;

@Repository
public class ElasticSearchDao {

    private TransportClient esClient;

    public ElasticSearchDao(@Value("${es.cluster.name}") String clusterName,
                            @Value("${es.hosts}") String esHost,
                            @Value("${es.cluster.port}") String esPort) {
        this.esClient = ElasticSearchHelper.getEsClient(clusterName, esHost, Integer.parseInt(esPort));
    }
    public synchronized SearchResponse[] dynamicNumberService(List<String> ipcIds) {
        BoolQueryBuilder ipcIdBQ = QueryBuilders.boolQuery();
        if (ipcIds != null && ipcIds.size() > 0) {
            for (String ipcId : ipcIds) {
                ipcIdBQ.should(QueryBuilders.matchPhraseQuery(DynamicTable.IPCID, ipcId).analyzer("standard"));
            }
        }
        String index = DynamicTable.DYNAMIC_INDEX;
        String type = DynamicTable.PERSON_INDEX_TYPE;
        SearchResponse[] responsesArray = new SearchResponse[2];
        // 统计所有抓拍总数
        SearchResponse responseV1 = esClient.prepareSearch(index).setTypes(type)
                .setQuery(QueryBuilders.boolQuery()
                        .must(ipcIdBQ))
                .setSize(1)
                .get();

        // 查询今天抓拍的数量
        SimpleDateFormat format = new SimpleDateFormat(EsSearchParam.TIMEFORMAT_YMDHMS);
        String endTime = format.format(System.currentTimeMillis());
        SearchResponse responseV2 = esClient.prepareSearch(index)
                .setQuery(QueryBuilders.boolQuery()
                        .must(ipcIdBQ)
                        .must(QueryBuilders.matchPhraseQuery(
                                DynamicTable.DATE, endTime.substring(0, endTime.indexOf(" ")))))
                .setSize(1)
                .get();
        responsesArray[0] = responseV1;
        responsesArray[1] = responseV2;
        return responsesArray;
    }

    public SearchResponse timeSoltNumber(List<String> ipcids, String startTime, String endTime) {
        SearchRequestBuilder searchRequestBuilder = esClient.prepareSearch(DynamicTable.DYNAMIC_INDEX)
                .setTypes(DynamicTable.PERSON_INDEX_TYPE)
                .setQuery(QueryBuilders.rangeQuery(DynamicTable.TIMESTAMP).gte(startTime).lte(endTime)).setSize(0);
        TermsAggregationBuilder teamAgg = AggregationBuilders.terms("ipc_count").field(DynamicTable.IPCID_KEYWORD);
        searchRequestBuilder.addAggregation(teamAgg);
        return searchRequestBuilder.execute().actionGet();
    }

    public SearchResponse CaptureCountSixHour(List<String> ipcIds, String startTime, String endTime) {
        BoolQueryBuilder ipcIdBQ = QueryBuilders.boolQuery();
        if (ipcIds != null && ipcIds.size() > 0) {
            for (String ipcId : ipcIds) {
                ipcIdBQ.should(QueryBuilders.matchQuery(DynamicTable.IPCID, ipcId).analyzer("standard"));
            }
        }
        SearchRequestBuilder searchRequestBuilder = esClient.prepareSearch(DynamicTable.DYNAMIC_INDEX)
                .setTypes(DynamicTable.PERSON_INDEX_TYPE)
                .setQuery(QueryBuilders.boolQuery()
                        .must(ipcIdBQ)
                        .must(QueryBuilders.rangeQuery(DynamicTable.TIMESTAMP).gte(startTime).lte(endTime)))
                .setSize(0);
        TermsAggregationBuilder teamAgg = AggregationBuilders.terms("ipc_count").field(DynamicTable.IPCID_KEYWORD);
        searchRequestBuilder.addAggregation(teamAgg);
        return searchRequestBuilder.execute().actionGet();
    }

    public SearchResponse getCaptureCount(String startTime, String endTime, List<String> ipcId) {
        BoolQueryBuilder totalBQ = QueryBuilders.boolQuery();
        // 开始时间和结束时间存在的时候的处理
        if (startTime != null && endTime != null && !startTime.equals("") && !endTime.equals("")) {
            totalBQ.must(QueryBuilders.rangeQuery(DynamicTable.TIMESTAMP).gte(startTime).lte(endTime));
        }
        BoolQueryBuilder ipcIdBQ = QueryBuilders.boolQuery();
        if (ipcId != null && ipcId.size() > 0) {
            for (String ipcid : ipcId) {
                ipcIdBQ.should(QueryBuilders.matchPhraseQuery(DynamicTable.IPCID, ipcid).analyzer("standard"));
            }
        }
        totalBQ.must(ipcIdBQ);
        return esClient.prepareSearch(DynamicTable.DYNAMIC_INDEX)
                .setTypes(DynamicTable.PERSON_INDEX_TYPE)
                .setQuery(totalBQ)
                .get();
    }

}
