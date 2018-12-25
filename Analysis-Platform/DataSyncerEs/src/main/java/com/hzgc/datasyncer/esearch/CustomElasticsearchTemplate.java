package com.hzgc.datasyncer.esearch;

import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.springframework.data.elasticsearch.ElasticsearchException;
import org.springframework.data.elasticsearch.annotations.Mapping;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.EntityMapper;
import org.springframework.data.elasticsearch.core.ResultsMapper;
import org.springframework.data.elasticsearch.core.convert.ElasticsearchConverter;
import org.springframework.data.elasticsearch.core.mapping.ElasticsearchPersistentEntity;
import org.springframework.data.elasticsearch.core.mapping.ElasticsearchPersistentProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Slf4j
public class CustomElasticsearchTemplate extends ElasticsearchTemplate {
    public CustomElasticsearchTemplate(Client client) {
        super(client);
    }

    public CustomElasticsearchTemplate(Client client, EntityMapper entityMapper) {
        super(client, entityMapper);
    }

    public CustomElasticsearchTemplate(Client client, ElasticsearchConverter elasticsearchConverter, EntityMapper entityMapper) {
        super(client, elasticsearchConverter, entityMapper);
    }

    public CustomElasticsearchTemplate(Client client, ResultsMapper resultsMapper) {
        super(client, resultsMapper);
    }

    public CustomElasticsearchTemplate(Client client, ElasticsearchConverter elasticsearchConverter) {
        super(client, elasticsearchConverter);
    }

    public CustomElasticsearchTemplate(Client client, ElasticsearchConverter elasticsearchConverter, ResultsMapper resultsMapper) {
        super(client, elasticsearchConverter, resultsMapper);
    }

    @Override
    public <T> boolean putMapping(Class<T> clazz) {
        if (clazz.isAnnotationPresent(Mapping.class)) {
            String mappingPath = clazz.getAnnotation(Mapping.class).mappingPath();
            if (StringUtils.hasText(mappingPath)) {
                String mappings = readFileFromClasspath(mappingPath);
                if (StringUtils.hasText(mappings)) {
                    return putMapping(clazz, mappings);
                }
            } else {
                log.info("mappingPath in @Mapping has to be defined. Building mappings using @Field");
            }
        }
        ElasticsearchPersistentEntity<T> persistentEntity = getPersistentEntityFor(clazz);
        XContentBuilder xContentBuilder = null;
        try {

            ElasticsearchPersistentProperty property = persistentEntity.getRequiredIdProperty();

            xContentBuilder = CustomMappingBuilder.buildMapping(clazz, persistentEntity.getIndexType(),
                    property.getFieldName(), persistentEntity.getParentType());
        } catch (Exception e) {
            throw new ElasticsearchException("Failed to build mapping for " + clazz.getSimpleName(), e);
        }
        return putMapping(clazz, xContentBuilder);
    }
}
