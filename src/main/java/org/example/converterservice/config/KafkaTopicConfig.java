package org.example.converterservice.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic fileConversionRequestedTopic() {
        return new NewTopic("file-conversion-requested", 1, (short) 1);
    }

    @Bean
    public NewTopic fileConversionCompletedTopic() {
        return new NewTopic("file-conversion-completed", 1, (short) 1);
    }

    @Bean
    public NewTopic fileConversionRequestedDltTopic() {
        return new NewTopic("file-conversion-requested-dlt", 1, (short) 1);
    }
}
