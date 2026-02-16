package com.nocode.ai.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    @Value("${spring.kafka.topics.social-messages:social-media-messages}")
    private String socialMessagesTopic;

    @Value("${spring.kafka.topics.analysis-results:analysis-results}")
    private String analysisResultsTopic;

    @Value("${spring.kafka.topics.alerts:brand-alerts}")
    private String alertsTopic;

    @Bean
    public NewTopic socialMessagesTopic() {
        return TopicBuilder.name(socialMessagesTopic)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic analysisResultsTopic() {
        return TopicBuilder.name(analysisResultsTopic)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic alertsTopic() {
        return TopicBuilder.name(alertsTopic)
                .partitions(1)
                .replicas(1)
                .build();
    }
}
