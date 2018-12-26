package com.hzgc.datasyncer.service;

import com.hzgc.datasyncer.bean.EsFaceObject;
import com.hzgc.datasyncer.esearch.CustomElasticsearchTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.query.IndexQuery;
import org.springframework.data.elasticsearch.core.query.IndexQueryBuilder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class EsIndexHelper {

    @Autowired
    @SuppressWarnings("unused")
    private CustomElasticsearchTemplate elasticsearchTemplate;

    public void bulkIndexFace(List<EsFaceObject> dataList, String indexName, String type, int patchSize) {
        int counter = 0;
        List<IndexQuery> queries = new ArrayList<>();
        for (EsFaceObject data : dataList) {
            queries.add(new IndexQueryBuilder()
                    .withId(data.getId())
                    .withObject(data)
                    .withIndexName(indexName)
                    .withType(type).build());
            if (counter % patchSize == 0) {
                elasticsearchTemplate.bulkIndex(queries);
                queries.clear();
                log.info("BulkIndex counter:{} reaching the upper limit, execute func:bulkIndex, " +
                        "index name:{}, type name:{}", patchSize, indexName, type);
            }
            counter++;
        }
        if (queries.size() > 0) {
            elasticsearchTemplate.bulkIndex(queries);
        }
        elasticsearchTemplate.refresh(indexName);
        log.info("BulkIndex completed, current patch size is:{}, index name is:{}, type name is:{}",
                dataList.size(), indexName, type);
    }
}
