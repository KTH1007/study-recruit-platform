package com.study.platform.global.outbox.model;

import java.time.LocalDateTime;
import java.util.List;

public interface OutboxEventRepository {

    OutboxEvent save(OutboxEvent event);
    List<OutboxEvent> findAllByStatusAndCreatedAtBefore(OutboxEventStatus status, LocalDateTime createdAt);
    int markSentById(Long id);
    void markFailedPermanently(Long id);
}
