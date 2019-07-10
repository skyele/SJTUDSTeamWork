package com.spark.mysql.repo;

import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

public class ApplicationStartup implements ApplicationListener<ContextRefreshedEvent> {
    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        SparkService bean = contextRefreshedEvent.getApplicationContext().getBean(SparkService.class);
        bean.foreachRDD();
    }
}
