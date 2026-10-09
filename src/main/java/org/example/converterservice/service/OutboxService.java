package org.example.converterservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.example.converterservice.dao.OutboxRepository;
import org.example.converterservice.event.FileConversionCompletedEvent;
import org.example.converterservice.kafka.KafkaProducerService;
import org.example.converterservice.model.entity.Outbox;
import org.example.converterservice.model.status.OutboxStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.example.converterservice.model.status.OutboxStatus.NEW;

@Service
@RequiredArgsConstructor
@Slf4j
public class OutboxService {
    private final OutboxRepository outboxRepository;
    private final KafkaProducerService kafkaProducerService;
    private final ObjectMapper objectMapper;

    @Scheduled(fixedDelay = 5000)
    @SchedulerLock(name = "outbox-publisher", lockAtMostFor = "10m", lockAtLeastFor = "1s")
    public void publishNewEvents() {
        List<Outbox> events = outboxRepository.findByStatus(NEW);

        for (Outbox outbox : events) {
            publishEvent(outbox);
        }
    }

    private void publishEvent(Outbox outbox) {
        try {
            FileConversionCompletedEvent event = objectMapper.readValue(
                    outbox.getPayload(), FileConversionCompletedEvent.class);

            kafkaProducerService.send(event);

            outbox.setStatus(OutboxStatus.PUBLISHED);
            outbox.setPublishedAt(LocalDateTime.now(ZoneOffset.UTC));

            outboxRepository.save(outbox);

            log.info("Событие {} опубликовано в Kafka", outbox.getEventId());

        } catch (Exception e) {
            log.error("Ошибка публикации события {} в Kafka", outbox.getEventId(), e);
        }
    }
}
