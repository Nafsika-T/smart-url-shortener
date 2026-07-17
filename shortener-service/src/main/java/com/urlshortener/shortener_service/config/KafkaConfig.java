package com.urlshortener.shortener_service.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    @Bean
    public NewTopic urlClicksTopic() {
        return TopicBuilder
                .name("url-clicks")
                .partitions(1)
                .replicas(1)
                .build();
    }
}