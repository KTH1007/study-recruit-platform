package com.study.platform.domain.notification.application

import com.study.platform.domain.notification.dto.response.NotificationResponse
import com.study.platform.domain.notification.event.NotificationEvent
import com.study.platform.global.constant.KafkaConstants
import com.study.platform.global.idempotency.IdempotencyStoragePort
import com.study.platform.global.redis.RedisMessagePublisher
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component
import tools.jackson.databind.ObjectMapper
import java.time.Duration

@Component
class NotificationKafkaConsumer(
    private val objectMapper: ObjectMapper,
    private val notificationProcessor: NotificationProcessor,
    private val redisMessagePublisher: RedisMessagePublisher,
    private val idempotencyStoragePort: IdempotencyStoragePort
) {
    private val log = LoggerFactory.getLogger(NotificationKafkaConsumer::class.java)!!

    companion object {
        private const val NOTIFICATION_CHANNEL = "notification"
        private const val IDEMPOTENCY_TTL_HOURS = 24L
    }

    @KafkaListener(topics = [KafkaConstants.NOTIFICATION_TOPIC], groupId = KafkaConstants.NOTIFICATION_GROUP)
    fun consume(payload: String, ack: Acknowledgment) {
        val event = objectMapper.readValue(payload, NotificationEvent::class.java)

        val idempotencyKey = "notification:processed:${event.outboxEventId}"
        val acquired = idempotencyStoragePort.setIfAbsent(idempotencyKey, "1", Duration.ofHours(IDEMPOTENCY_TTL_HOURS))
        if (!acquired) {
            log.info("중복 메시지 skip - outboxEventId={}", event.outboxEventId)
            ack.acknowledge()
            return
        }

        val notification = try {
            notificationProcessor.process(event)
        } catch (e: Exception) {
            // process() 실패 시점엔 아직 커밋된 것이 없으므로 idempotency key를 지워 재처리를 허용한다.
            idempotencyStoragePort.delete(idempotencyKey)
            throw e
        }

        if (notification != null) {
            try {
                redisMessagePublisher.publish(NOTIFICATION_CHANNEL, objectMapper.writeValueAsString(NotificationResponse.from(notification)))
            } catch (e: Exception) {
                // Notification은 이미 커밋되었으므로 key를 지우면 재처리 시 중복 row가 생긴다. 발행 실패는 로그만 남긴다.
                log.error("알림 Redis publish 실패 - notificationId={}", notification.id, e)
            }
        }
        ack.acknowledge()
    }
}
