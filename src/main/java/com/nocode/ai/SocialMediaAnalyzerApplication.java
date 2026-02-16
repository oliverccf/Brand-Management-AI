package com.nocode.ai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableCaching
@EnableKafka
@EnableAsync
public class SocialMediaAnalyzerApplication {

    public SocialMediaAnalyzerApplication() {
        super();
    }


    public static void main(String[] args) {
        SpringApplication.run(SocialMediaAnalyzerApplication.class, args);
    }
}