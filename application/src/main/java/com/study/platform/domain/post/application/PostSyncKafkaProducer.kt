package com.study.platform.domain.post.application

import com.study.platform.domain.post.event.PostSyncEvent
import com.study.platform.global.constant.KafkaConstants
import com.study.platform.global.kafka.KafkaMessagePublisher
import com.study.platform.global.outbox.application.OutboxEventService
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener
import tools.jackson.databind.ObjectMapper

@Component
class PostSyncKafkaProducer(
    private val kafkaMessagePublisher: KafkaMessagePublisher,
    private val objectMapper: ObjectMapper,
    private val outboxEventService: OutboxEventService
) {
    private val log = LoggerFactory.getLogger(PostSyncKafkaProducer::class.java)!!

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun handle(event: PostSyncEvent) {
        val payload = objectMapper.writeValueAsString(event)
        kafkaMessagePublisher.publish(KafkaConstants.POST_SYNC_TOPIC, event.postId.toString(), payload)
            .whenComplete { _, ex ->
                if (ex == null) {
                    outboxEventService.markSent(event.outboxEventId)
                    log.info("ES 동기화 이벤트 발행 성공 - postId: {}, type: {}", event.postId, event.operationType)
                } else {
                    log.warn("ES 동기화 이벤트 발행 실패 - outbox 스케줄러가 재시도 예정. postId: {}, type: {}", event.postId, event.operationType, ex)
                }
            }
    }
}
