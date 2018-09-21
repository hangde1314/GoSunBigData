package com.hzgc.service.people;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
@EnableEurekaClient
@MapperScan(value = "com.hzgc.service.people.dao")
public class PeopleApplication {
    public static void main(String[] args) {
        ConfigurableApplicationContext abc = SpringApplication.run(PeopleApplication.class, args);
        System.out.println(abc.getBean(ABCConfig.class).getAbc());

    }

}

