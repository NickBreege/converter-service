package org.example.converterservice.dao;

import org.example.converterservice.model.entity.Outbox;
import org.example.converterservice.model.status.OutboxStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OutboxRepository extends JpaRepository<Outbox, Long> {
    List<Outbox> findByStatus(OutboxStatus status);
}