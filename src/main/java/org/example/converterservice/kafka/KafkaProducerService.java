package org.example.converterservice.kafka;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.example.converterservice.event.FileConversionCompletedEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaProducerService {
    private final KafkaTemplate<String, FileConversionCompletedEvent> kafkaTemplate;
    @Value("${app.kafka.topics.completed}")
    private String completedTopic;

    @SneakyThrows
    public void send(FileConversionCompletedEvent event) {
        try {
            kafkaTemplate.send(completedTopic, event.eventId().toString(), event).get();
            log.info("Событие {} успешно отправлено в Kafka. Топик: {}", event.eventId(), completedTopic);

        } catch (ExecutionException e) {
            log.error("Ошибка отправки события {} в Kafka. Топик: {}", event.eventId(), completedTopic, e);
            throw e;
        }
    }
}
