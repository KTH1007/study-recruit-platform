package com.study.platform.domain.post.application

import com.study.platform.domain.post.event.PostSyncEvent
import com.study.platform.domain.post.model.FailedPostSync
import com.study.platform.domain.post.model.FailedPostSyncRepository
import com.study.platform.global.constant.KafkaConstants
import com.study.platform.global.kafka.KafkaMessagePublisher
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.handler.annotation.Header
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import tools.jackson.databind.ObjectMapper

@Component
class PostSyncDltConsumer(
    private val objectMapper: ObjectMapper,
    private val kafkaMessagePublisher: KafkaMessagePublisher,
    private val failedPostSyncRepository: FailedPostSyncRepository
) {
    private val log = LoggerFactory.getLogger(PostSyncDltConsumer::class.java)!!

    @Transactional
    @KafkaListener(topics = [KafkaConstants.POST_SYNC_DLT_TOPIC], groupId = KafkaConstants.POST_SYNC_DLT_GROUP)
    fun consume(
        payload: String,
        ack: Acknowledgment,
        @Header(name = KafkaHeaders.EXCEPTION_MESSAGE, required = false) exceptionMessage: String?
    ) {
        try {
            val event = objectMapper.readValue(payload, PostSyncEvent::class.java)
            log.error(
                "DLT 수신 - postId={}, type={}, retryCount={}, 원인={}",
                event.postId, event.operationType, event.retryCount, exceptionMessage
            )

            if (event.retryCount < KafkaConstants.MAX_DLT_RETRY) {
                kafkaMessagePublisher.publish(KafkaConstants.POST_SYNC_TOPIC, objectMapper.writeValueAsString(event.withRetry())).get(5, java.util.concurrent.TimeUnit.SECONDS)
                log.info("post-sync 토픽 재투입 - retryCount={}", event.retryCount + 1)
            } else {
                failedPostSyncRepository.save(FailedPostSync.from(event, exceptionMessage ?: "unknown"))
                log.error("최대 재시도 초과 - DB 영구 저장. postId={}", event.postId)
            }
        } catch (e: Exception) {
            log.error("DLT 페이로드 파싱 실패 - payload: {}", payload, e)
        } finally {
            ack.acknowledge()
        }
    }
}
