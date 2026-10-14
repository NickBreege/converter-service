package org.example.converterservice.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaTopicConfig {
    @Value("${app.kafka.topics.requested}")
    private String requestedTopic;
    @Value("${app.kafka.topics.completed}")
    private String completedTopic;
    @Value("${app.kafka.topics.requested-dlt}")
    private String requestedDltTopic;

    @Bean
    public NewTopic fileConversionRequestedTopic() {
        return new NewTopic(requestedTopic, 1, (short) 1);
    }

    @Bean
    public NewTopic fileConversionCompletedTopic() {
        return new NewTopic(completedTopic, 1, (short) 1);
    }

    @Bean
    public NewTopic fileConversionRequestedDltTopic() {
        return new NewTopic(requestedDltTopic, 1, (short) 1);
    }
}
