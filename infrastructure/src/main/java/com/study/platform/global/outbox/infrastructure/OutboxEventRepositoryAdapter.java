package com.study.platform.global.outbox.infrastructure;

import com.study.platform.global.outbox.model.OutboxEvent;
import com.study.platform.global.outbox.model.OutboxEventRepository;
import com.study.platform.global.outbox.model.OutboxEventStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class OutboxEventRepositoryAdapter implements OutboxEventRepository {

    private final OutboxEventJpaRepository outboxEventJpaRepository;

    @Override
    public OutboxEvent save(OutboxEvent event) {
        return outboxEventJpaRepository.save(event);
    }

    @Override
    public List<OutboxEvent> findAllByStatusAndCreatedAtBefore(OutboxEventStatus status, LocalDateTime createdAt) {
        return outboxEventJpaRepository.findAllByStatusAndCreatedAtBefore(status, createdAt);
    }

    @Override
    public int markSentById(Long id) {
        return outboxEventJpaRepository.markSentById(id);
    }

    @Override
    public void markFailedPermanently(Long id) {
        outboxEventJpaRepository.markFailedPermanently(id);
    }
}