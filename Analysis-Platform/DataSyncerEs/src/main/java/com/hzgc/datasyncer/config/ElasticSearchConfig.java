package com.hzgc.datasyncer.config;

import com.hzgc.datasyncer.esearch.CustomElasticsearchTemplate;
import org.elasticsearch.client.Client;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.data.elasticsearch.ElasticsearchAutoConfiguration;
import org.springframework.boot.autoconfigure.data.elasticsearch.ElasticsearchDataAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.convert.ElasticsearchConverter;

@Configuration
//@ConditionalOnClass({Client.class, ElasticsearchTemplate.class})
//@AutoConfigureAfter(ElasticsearchAutoConfiguration.class)
public class ElasticSearchConfig extends ElasticsearchDataAutoConfiguration {
    @Bean
//    @ConditionalOnMissingBean
//    @ConditionalOnBean(Client.class)
    public CustomElasticsearchTemplate elasticsearchTemplate(Client client, ElasticsearchConverter converter) {
        try {
            return new CustomElasticsearchTemplate(client, converter);
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }
}
