package org.example.converterservice.service;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.example.converterservice.converter.Converter;
import org.example.converterservice.dao.InboxRepository;
import org.example.converterservice.dao.OutboxRepository;
import org.example.converterservice.event.FileConversionCompletedEvent;
import org.example.converterservice.event.FileConversionRequestedEvent;
import org.example.converterservice.model.entity.Inbox;
import org.example.converterservice.model.entity.Outbox;
import org.example.converterservice.model.status.InboxStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import tools.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import static org.example.converterservice.model.status.InboxStatus.*;
import static org.example.converterservice.model.status.OutboxStatus.NEW;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConversionService {
    private final MinioService minioService;
    private final List<Converter> converters;
    private final ObjectMapper objectMapper;
    private final TransactionTemplate transactionTemplate;
    private final InboxRepository inboxRepository;
    private final OutboxRepository outboxRepository;

    @SneakyThrows
    public void process(FileConversionRequestedEvent event) {
        UUID eventId = event.eventId();

        log.info("CONVERSION - начало обработки события. eventId={}, filePath={}", eventId, event.payload().filePath());

        Inbox inbox = inboxRepository.findByEventId(eventId).orElseGet(() -> createInbox(eventId));

        log.info("INBOX - событие получено. eventId={}, status={}", eventId, inbox.getStatus());

        if (inbox.getStatus() == SUCCESS) {
            log.info("INBOX - событие уже успешно обработано, пропускаем. eventId={}", eventId);
            return;
        }

        String filePath = event.payload().filePath();

        try (InputStream inputStream = minioService.download("converter-files", filePath)) {
            String fileType = getFileType(filePath);

            log.info("CONVERTER - определён тип файла. eventId={}, filePath={}, fileType={}",
                    eventId,
                    filePath,
                    fileType);

            Converter converter = getConverter(fileType);

            log.info("CONVERTER - выбран конвертер. eventId={}, fileType={}, converter={}",
                    eventId,
                    fileType,
                    converter.getClass().getSimpleName());

            byte[] pdf = converter.convert(inputStream);

            log.info("CONVERTER - конвертация завершена. eventId={}, filePath={}, pdfSize={} bytes",
                    eventId,
                    filePath,
                    pdf.length);

            String resultFilePath = filePath + ".pdf";

            minioService.upload("converter-files", resultFilePath, pdf);

            FileConversionCompletedEvent completedEvent = new FileConversionCompletedEvent(
                    eventId,
                    "FILE_CONVERSION_COMPLETED",
                    OffsetDateTime.now(ZoneOffset.UTC),
                    new FileConversionCompletedEvent.Payload(resultFilePath));

            transactionTemplate.executeWithoutResult(status -> {
                saveToOutbox(completedEvent);
                updateInboxStatus(inbox, SUCCESS);
            });

            log.info("CONVERSION - обработка успешно завершена. eventId={}, sourceFile={}, resultFile={}",
                    eventId,
                    filePath,
                    resultFilePath);

        } catch (Exception e) {
            updateInboxStatus(inbox, FAILED);
            log.error("CONVERSION - ошибка при обработке события. eventId={}, filePath={}", eventId, filePath, e);
            throw e;
        }
    }

    private Converter getConverter(String fileType) {
        return converters.stream()
                .filter(converter -> converter.supports(fileType))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Неподдерживаемый тип файла: " + fileType));
    }

    private String getFileType(String filePath) {
        int lastDotIndex = filePath.lastIndexOf('.');

        if (lastDotIndex == -1) {
            throw new IllegalArgumentException("У файла отсутствует расширение: " + filePath);
        }

        return filePath.substring(lastDotIndex + 1).toLowerCase();
    }

    private Inbox createInbox(UUID eventId) {
        Inbox inbox = new Inbox();
        inbox.setEventId(eventId);
        inbox.setStatus(RECEIVED);
        inbox.setCreatedAt(LocalDateTime.now(ZoneOffset.UTC));

        return inboxRepository.save(inbox);
    }

    private void updateInboxStatus(Inbox inbox, InboxStatus status) {
        inbox.setStatus(status);

        if (status == SUCCESS || status == FAILED) {
            inbox.setProcessedAt(LocalDateTime.now(ZoneOffset.UTC));
        }

        inboxRepository.save(inbox);
    }

    private void saveToOutbox(FileConversionCompletedEvent event) {
        Outbox outbox = new Outbox();

        outbox.setEventId(event.eventId());
        outbox.setEventType(event.eventType());
        outbox.setPayload(objectMapper.writeValueAsString(event));
        outbox.setStatus(NEW);
        outbox.setCreatedAt(LocalDateTime.now(ZoneOffset.UTC));

        outboxRepository.save(outbox);

        log.info("Событие {} сохранено в Outbox", event.eventId());
    }
}
