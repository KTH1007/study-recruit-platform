package com.study.platform.domain.notification.application

import com.study.platform.domain.notification.dto.response.NotificationResponse
import com.study.platform.domain.notification.model.NotificationEvent
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

        try {
            val notification = notificationProcessor.process(event)
            ack.acknowledge()
            if (notification != null) {
                redisMessagePublisher.publish(NOTIFICATION_CHANNEL, objectMapper.writeValueAsString(NotificationResponse.from(notification)))
            }
        } catch (e: Exception) {
            idempotencyStoragePort.delete(idempotencyKey)
            throw e
        }
    }
}
