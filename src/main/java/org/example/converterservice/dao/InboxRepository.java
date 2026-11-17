package org.example.converterservice.dao;

import org.example.converterservice.model.entity.Inbox;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface InboxRepository extends JpaRepository<Inbox, Long> {
    Optional<Inbox> findByEventId(UUID eventId);
}