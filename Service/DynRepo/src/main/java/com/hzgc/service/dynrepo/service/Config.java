package com.hzgc.service.dynrepo.service;

import com.hzgc.collect.zk.register.RegisterWatcher;
import com.hzgc.common.attribute.service.AttributeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.web.client.RestTemplate;

@Configuration
public class Config {
    @Autowired
    @SuppressWarnings("unused")
    private Environment environment;

    @Bean
    @LoadBalanced
    RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    AttributeService attributeService() {
        return new AttributeService();
    }

    @Bean
    public RegisterWatcher registerWatcher() {
        return new RegisterWatcher(environment.getProperty("zk.address"));
    }
}
