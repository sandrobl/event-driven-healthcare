package com.eventdriven.healthcare.patientcheckin.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Value("${spring.kafka.patientStatic-topic}")
    private String patientStaticTopic;

    @Value("${spring.kafka.patientMetrics-topic}")
    private String patientMetricsTopic;

    @Bean
    public NewTopic patientStaticTopic() {
        return TopicBuilder.name(patientStaticTopic)
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic patientMetricsTopic() {
        return TopicBuilder.name(patientMetricsTopic)
                .partitions(1)
                .replicas(1)
                .build();
    }
}