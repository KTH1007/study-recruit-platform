package com.study.platform.global.outbox.application;

import com.study.platform.global.outbox.model.OutboxEvent;
import com.study.platform.global.outbox.model.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OutboxEventService {

    private final OutboxEventRepository outboxEventRepository;

    @Transactional(propagation = Propagation.MANDATORY)
    public Long save(String topic, String messageKey, String payload) {
        return outboxEventRepository.save(OutboxEvent.pending(topic, messageKey, payload)).getId();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Long saveWithNewTx(String topic, String messageKey, String payload) {
        return outboxEventRepository.save(OutboxEvent.pending(topic, messageKey, payload)).getId();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markSent(Long id) {
        outboxEventRepository.markSentById(id);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markFailedPermanently(Long id) {
        outboxEventRepository.markFailedPermanently(id);
    }
}