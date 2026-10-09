package org.example.converterservice.model.status;

public enum OutboxStatus {
    NEW, //ещё не отправили
    PUBLISHED, //успешно отправили
    FAILED //произошла ошибка отправки
}
