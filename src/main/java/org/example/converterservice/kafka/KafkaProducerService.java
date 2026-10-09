package org.example.converterservice.kafka;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.example.converterservice.event.FileConversionCompletedEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaProducerService {
    private final KafkaTemplate<String, FileConversionCompletedEvent> kafkaTemplate;

    @SneakyThrows
    public void send(FileConversionCompletedEvent event) {
        try {
            kafkaTemplate.send("file-conversion-completed", event.eventId().toString(), event).get();
            log.info("Событие {} успешно отправлено в Kafka. Топик: file-conversion-completed", event.eventId());

        } catch (ExecutionException e) {
            log.error("Ошибка отправки события {} в Kafka. Топик: file-conversion-completed", event.eventId(), e);
            throw e;
        }
    }
}
