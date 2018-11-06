package com.hzgc.service;

import com.hzgc.common.service.api.config.EnableInnerService;
import com.hzgc.common.service.api.config.EnablePlatformService;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableEurekaClient
@EnablePlatformService
@EnableTransactionManagement
@EnableInnerService
@EnableKafka
@MapperScan(value = "com.hzgc.service.*.dao")
public class DispatchApplication {
    public static void main(String[] args) {
        SpringApplication.run(DispatchApplication.class, args);
    }
}
