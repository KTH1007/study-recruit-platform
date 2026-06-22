package com.study.platform.global.outbox.application

import com.study.platform.global.outbox.model.OutboxEvent
import com.study.platform.global.outbox.model.OutboxEventRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@Service
class OutboxEventService(
    private val outboxEventRepository: OutboxEventRepository
) {

    @Transactional(propagation = Propagation.MANDATORY)
    fun save(topic: String, messageKey: String, payload: String): Long {
        return outboxEventRepository.save(OutboxEvent.pending(topic, messageKey, payload)).id!!
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun saveWithNewTx(topic: String, messageKey: String, payload: String): Long {
        return outboxEventRepository.save(OutboxEvent.pending(topic, messageKey, payload)).id!!
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun markSent(id: Long) {
        outboxEventRepository.markSentById(id)
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun markFailedPermanently(id: Long) {
        outboxEventRepository.markFailedPermanently(id)
    }
}
