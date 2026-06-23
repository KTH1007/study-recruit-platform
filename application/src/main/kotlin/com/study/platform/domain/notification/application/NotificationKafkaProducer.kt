package com.study.platform.domain.notification.application

import com.study.platform.domain.notification.model.NotificationEvent
import com.study.platform.domain.notification.model.NotificationType
import com.study.platform.global.constant.KafkaConstants
import com.study.platform.global.kafka.KafkaMessagePublisher
import com.study.platform.global.outbox.application.OutboxEventService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import tools.jackson.databind.ObjectMapper
import java.util.UUID

private val log = LoggerFactory.getLogger(NotificationKafkaProducer::class.java)!!

@Component
class NotificationKafkaProducer(
    private val kafkaMessagePublisher: KafkaMessagePublisher,
    private val objectMapper: ObjectMapper,
    private val outboxEventService: OutboxEventService
) : com.study.platform.domain.notification.model.NotificationPublisher {

    override fun send(outboxEventId: Long, receiverId: UUID, type: NotificationType, message: String, targetId: UUID?) {
        val event = NotificationEvent(receiverId, type, message, targetId, 0)
        val payload = objectMapper.writeValueAsString(event)
        kafkaMessagePublisher.publish(KafkaConstants.NOTIFICATION_TOPIC, receiverId.toString(), payload)
            .whenComplete { _, ex ->
                if (ex == null) {
                    outboxEventService.markSent(outboxEventId)
                    log.info("알림 이벤트 발행 성공 - receiverId: {}, type: {}", receiverId, type)
                } else {
                    log.warn("알림 이벤트 발행 실패 - outbox 스케줄러가 재시도 예정. receiverId: {}, type: {}", receiverId, type, ex)
                }
            }
    }
}
