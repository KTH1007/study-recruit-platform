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

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun incrementRetryCount(id: Long) {
        val outbox = outboxEventRepository.findById(id) ?: return
        outbox.incrementRetryCount()
        outboxEventRepository.save(outbox)
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun saveWithIdEmbedded(topic: String, key: String, buildPayload: (Long) -> String): Long {
        val outbox = outboxEventRepository.save(OutboxEvent.pending(topic, key, "{}"))
        val id = outbox.id!!
        outbox.payload = buildPayload(id)
        return id
    }

    @Transactional(propagation = Propagation.MANDATORY)
    fun saveWithIdEmbeddedInTx(topic: String, key: String, buildPayload: (Long) -> String): Long {
        val outbox = outboxEventRepository.save(OutboxEvent.pending(topic, key, "{}"))
        val id = outbox.id!!
        outbox.payload = buildPayload(id)
        return id
    }
}
