package com.bondtradex.ioi.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.config.TopicConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

import java.util.Map;

@Configuration
public class KafkaConfig {

    @Bean
    public NewTopic ioiEventsTopic(
            @Value("${app.kafka.topics.ioi-events}")
            String topicName
    ) {
        return TopicBuilder
                .name(topicName)
                .partitions(3)
                .replicas(3)
                .configs(Map.of(
                        TopicConfig.MIN_IN_SYNC_REPLICAS_CONFIG,
                        "2"
                ))
                .build();
    }

    @Bean
    public NewTopic deadLetterTopic() {

        return TopicBuilder
                .name("ioi-events-dlt")
                .partitions(3)
                .replicas(3)
                .build();
    }

    @Bean
    public NewTopic replayTopic() {

        return TopicBuilder
                .name("ioi-events-replay")
                .partitions(3)
                .replicas(3)
                .build();
    }
}