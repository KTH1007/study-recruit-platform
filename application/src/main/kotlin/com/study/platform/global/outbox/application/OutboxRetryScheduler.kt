package com.study.platform.global.outbox.application

import com.study.platform.domain.post.event.PostSyncEvent
import com.study.platform.domain.post.model.FailedPostSync
import com.study.platform.domain.post.model.FailedPostSyncRepository
import com.study.platform.global.constant.KafkaConstants
import com.study.platform.global.kafka.KafkaMessagePublisher
import com.study.platform.global.outbox.model.OutboxEvent
import com.study.platform.global.outbox.model.OutboxEventRepository
import com.study.platform.global.outbox.model.OutboxEventStatus
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock
import org.slf4j.LoggerFactory
import org.springframework.data.domain.PageRequest
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import tools.jackson.databind.ObjectMapper
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

@Component
class OutboxRetryScheduler(
    private val outboxEventRepository: OutboxEventRepository,
    private val outboxEventService: OutboxEventService,
    private val kafkaMessagePublisher: KafkaMessagePublisher,
    private val failedPostSyncRepository: FailedPostSyncRepository,
    private val objectMapper: ObjectMapper
) {
    private val log = LoggerFactory.getLogger(OutboxRetryScheduler::class.java)!!

    companion object {
        private const val MAX_OUTBOX_RETRY = 3
    }

    @Scheduled(fixedDelay = 30_000)
    @SchedulerLock(name = "OutboxRetryScheduler", lockAtMostFor = "25s")
    fun retryPendingEvents() {
        val pendingEvents = outboxEventRepository
            .findAllByStatusAndCreatedAtBefore(OutboxEventStatus.PENDING, LocalDateTime.now().minusSeconds(30), PageRequest.of(0, 100))

        for (outbox in pendingEvents) {
            retry(outbox)
        }
    }

    private fun retry(outbox: OutboxEvent) {
        try {
            kafkaMessagePublisher.publish(outbox.topic, outbox.messageKey, outbox.payload)
                .get(5, TimeUnit.SECONDS)
            outboxEventService.markSent(outbox.id!!)
            log.info("Outbox 재발행 성공 - id: {}, topic: {}", outbox.id, outbox.topic)
        } catch (e: Exception) {
            log.warn("Outbox 재발행 실패 - id: {}, topic: {}", outbox.id, outbox.topic, e)
            handleRetryFailure(outbox.id!!, outbox.topic, outbox.payload, outbox.retryCount, e)
        }
    }

    @Transactional
    fun handleRetryFailure(outboxId: Long, topic: String, payload: String, retryCount: Int, e: Exception) {
        if (retryCount >= MAX_OUTBOX_RETRY) {
            log.error("Outbox 최대 재시도 초과 - id: {}, topic: {}", outboxId, topic)
            if (topic == KafkaConstants.POST_SYNC_TOPIC) {
                val event = objectMapper.readValue(payload, PostSyncEvent::class.java)
                failedPostSyncRepository.save(FailedPostSync.from(event, e.message ?: "unknown"))
            }
            outboxEventService.markFailedPermanently(outboxId)
        } else {
            outboxEventService.incrementRetryCount(outboxId)
        }
    }
}
