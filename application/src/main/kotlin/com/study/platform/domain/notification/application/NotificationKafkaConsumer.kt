package com.study.platform.domain.notification.application

import com.study.platform.domain.notification.dto.response.NotificationResponse
import com.study.platform.domain.notification.model.FailedNotification
import com.study.platform.domain.notification.model.FailedNotificationRepository
import com.study.platform.domain.notification.model.Notification
import com.study.platform.domain.notification.model.NotificationEvent
import com.study.platform.domain.notification.model.NotificationRepository
import com.study.platform.domain.user.model.UserRepository
import com.study.platform.global.constant.KafkaConstants
import com.study.platform.global.exception.CustomException
import com.study.platform.global.exception.ErrorCode
import com.study.platform.global.redis.RedisMessagePublisher
import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import tools.jackson.databind.ObjectMapper
import java.time.Duration

@Component
class NotificationKafkaConsumer(
    private val objectMapper: ObjectMapper,
    private val userRepository: UserRepository,
    private val notificationRepository: NotificationRepository,
    private val redisMessagePublisher: RedisMessagePublisher,
    private val failedNotificationRepository: FailedNotificationRepository,
    private val stringRedisTemplate: StringRedisTemplate
) {
    private val log = LoggerFactory.getLogger(NotificationKafkaConsumer::class.java)!!

    companion object {
        private const val NOTIFICATION_CHANNEL = "notification"
        private const val IDEMPOTENCY_TTL_HOURS = 24L
    }

    @Transactional
    @KafkaListener(topics = [KafkaConstants.NOTIFICATION_TOPIC], groupId = KafkaConstants.NOTIFICATION_GROUP)
    fun consume(payload: String, ack: Acknowledgment) {
        val event = objectMapper.readValue(payload, NotificationEvent::class.java)

        val idempotencyKey = "notification:processed:${event.outboxEventId}"
        val acquired = stringRedisTemplate.opsForValue()
            .setIfAbsent(idempotencyKey, "1", Duration.ofHours(IDEMPOTENCY_TTL_HOURS))
        if (acquired != true) {
            log.info("중복 메시지 skip - outboxEventId={}", event.outboxEventId)
            ack.acknowledge()
            return
        }

        if (event.retryCount >= KafkaConstants.MAX_DLT_RETRY) {
            failedNotificationRepository.save(FailedNotification.from(event, "메인 Consumer 최종 실패"))
            ack.acknowledge()
            return
        }
        val receiver = userRepository.findById(event.receiverId)
            ?: throw CustomException(ErrorCode.USER_NOT_FOUND)
        val notification = Notification.create(receiver, event.type, event.message, event.targetId)
        notificationRepository.save(notification)
        ack.acknowledge()
        redisMessagePublisher.publish(NOTIFICATION_CHANNEL, objectMapper.writeValueAsString(NotificationResponse.from(notification)))
    }
}
