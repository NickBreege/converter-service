package org.example.converterservice.kafka;

import lombok.RequiredArgsConstructor;
import org.example.converterservice.event.FileConversionRequestedEvent;
import org.example.converterservice.service.ConversionService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FileConversionConsumer {
    private final ConversionService conversionService;

    @KafkaListener(topics = "${app.kafka.topics.requested}")
    public void consume(FileConversionRequestedEvent event) {
        conversionService.process(event);
    }
}
