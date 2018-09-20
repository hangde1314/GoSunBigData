package com.hzgc.service.dyncar.dao;

import com.hzgc.common.util.es.ElasticSearchHelper;
import com.hzgc.common.service.facedynrepo.VehicleTable;
import com.hzgc.service.dyncar.bean.CaptureOption;
import com.hzgc.service.dyncar.bean.VehicleAttribute;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;

@Slf4j
@Repository
public class ElasticSearchDao {
    private TransportClient esClient;


    public ElasticSearchDao(@Value("${es.cluster.name}") String clusterName,
                            @Value("${es.hosts}") String esHost,
                            @Value("${es.cluster.port}") String esPort) {
        this.esClient = ElasticSearchHelper.getEsClient(clusterName, esHost, Integer.parseInt(esPort));
    }

    //返回查询结果
    public SearchResponse getCaptureHistory(CaptureOption option, String sortParam) {
        BoolQueryBuilder queryBuilder = createBoolQueryBuilder(option);

        SearchRequestBuilder requestBuilder = createSearchRequestBuilder()
                .setQuery(queryBuilder)
                .setFrom(option.getStart())
                .setSize(option.getLimit())
                .addSort(VehicleTable.TIMESTAMP,
                        Objects.equals(sortParam, EsSearchParam.DESC) ? SortOrder.DESC : SortOrder.ASC);
        return requestBuilder.get();
    }

    //根据多个ipcid进行查询总共的
    public SearchResponse getCaptureHistory(CaptureOption option, List<String> ipcList, String sortParam) {
        BoolQueryBuilder queryBuilder = createBoolQueryBuilder(option);
        setDeviceIdList(queryBuilder, ipcList);
        SearchRequestBuilder requestBuilder = createSearchRequestBuilder()
                .setQuery(queryBuilder)
                .setFrom(option.getStart())
                .setSize(option.getLimit())
                .addSort(VehicleTable.TIMESTAMP,
                        Objects.equals(sortParam, EsSearchParam.DESC) ? SortOrder.DESC : SortOrder.ASC);
        return requestBuilder.get();
    }

    //根据单个ipcid进行查询
    public SearchResponse getCaptureHistory(CaptureOption option, String ipc, String sortParam) {
        BoolQueryBuilder queryBuilder = createBoolQueryBuilder(option);
        setDeviceId(queryBuilder, ipc);
        SearchRequestBuilder requestBuilder = createSearchRequestBuilder()
                .setQuery(queryBuilder)
                .setFrom(option.getStart())
                .setSize(option.getLimit())
                .addSort(VehicleTable.TIMESTAMP,
                        Objects.equals(sortParam, EsSearchParam.DESC) ? SortOrder.DESC : SortOrder.ASC);
        return requestBuilder.get();
    }

    //设置index和type
    private SearchRequestBuilder createSearchRequestBuilder() {
        return esClient.prepareSearch(VehicleTable.INDEX)
                .setTypes(VehicleTable.TYPE);
    }

    //创建boolQuery对象
    private BoolQueryBuilder createBoolQueryBuilder(CaptureOption option) {
        // 最终封装成的boolQueryBuilder 对象。
        BoolQueryBuilder totalBQ = QueryBuilders.boolQuery();
        //筛选车辆属性
        if (option.getAttributes() != null && option.getAttributes().size() > 0) {
            setAttribute(totalBQ, option);
        }

        // 开始时间和结束时间存在的时候的处理
        if (option.getStartTime() != null && option.getEndTime() != null &&
                !option.getStartTime().equals("") && !option.getEndTime().equals("")) {
            setStartEndTime(totalBQ, option.getStartTime(), option.getEndTime());
        }
        return totalBQ;
    }

    //时间过滤
    private void setStartEndTime(BoolQueryBuilder totalBQ, String startTime, String endTime) {
        totalBQ.must(QueryBuilders.rangeQuery(VehicleTable.TIMESTAMP).gte(startTime).lte(endTime));
    }

    //多个ipcid查询
    private void setDeviceIdList(BoolQueryBuilder totalBQ, List<String> deviceId) {
        // 设备ID 的的boolQueryBuilder
        BoolQueryBuilder devicdIdBQ = QueryBuilders.boolQuery();
        for (Object t : deviceId) {
            devicdIdBQ.should(QueryBuilders.matchPhraseQuery(VehicleTable.IPCID, t));
        }
        totalBQ.must(devicdIdBQ);
    }

    //单个ipcid查询
    private void setDeviceId(BoolQueryBuilder totalBQ, String ipc) {
        BoolQueryBuilder deviceIdBQ = QueryBuilders.boolQuery();
        deviceIdBQ.should(QueryBuilders.matchPhraseQuery(VehicleTable.IPCID, ipc));
        totalBQ.must(deviceIdBQ);
    }

    //车辆属性过滤
    private void setAttribute(BoolQueryBuilder totalBQ, CaptureOption option) {
        String brand = option.getBrand_name();
        String pLicence = option.getPlate_licence();
        //车牌模糊搜
        if (null != pLicence && pLicence.length() > 0) {
            BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
            boolQueryBuilder.should(QueryBuilders.queryStringQuery(VehicleTable.PLATE_LICENCE + ":*" + pLicence + "*"));
            totalBQ.must(boolQueryBuilder);
        }
        //车标分词搜索
        if (null != brand && pLicence.length() > 0) {
            BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
            boolQueryBuilder.should(QueryBuilders.matchQuery(VehicleTable.BRAND_NAME, brand).analyzer(VehicleTable.IK_SMART));
            totalBQ.must(boolQueryBuilder);
        }
        List <VehicleAttribute> attributes = option.getAttributes();
        if (null != attributes && attributes.size() > 0){
            for (VehicleAttribute attribute : attributes) {
                String attributeName = attribute.getAttributeName();
                List <String> attributeValues = attribute.getAttributeCodes();
                BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
                if (null != attributeValues && attributeValues.size() > 0) {
                    for (String code : attributeValues) {
                        boolQueryBuilder.should(QueryBuilders.matchQuery(attributeName, code));
                    }
                }
                totalBQ.must(boolQueryBuilder);
            }
        }
    }
}