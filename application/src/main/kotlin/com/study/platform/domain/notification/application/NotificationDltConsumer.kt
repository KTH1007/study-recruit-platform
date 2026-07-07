package com.study.platform.domain.notification.application

import com.study.platform.domain.notification.model.FailedNotification
import com.study.platform.domain.notification.model.FailedNotificationRepository
import com.study.platform.domain.notification.model.NotificationEvent
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
class NotificationDltConsumer(
    private val objectMapper: ObjectMapper,
    private val kafkaMessagePublisher: KafkaMessagePublisher,
    private val failedNotificationRepository: FailedNotificationRepository
) {
    private val log = LoggerFactory.getLogger(NotificationDltConsumer::class.java)!!

    @Transactional
    @KafkaListener(topics = [KafkaConstants.NOTIFICATION_DLT_TOPIC], groupId = KafkaConstants.NOTIFICATION_DLT_GROUP)
    fun consume(
        payload: String,
        ack: Acknowledgment,
        @Header(name = KafkaHeaders.EXCEPTION_MESSAGE, required = false) exceptionMessage: String?
    ) {
        try {
            val event = objectMapper.readValue(payload, NotificationEvent::class.java)
            log.error(
                "DLT 수신 - receiverId={}, type={}, retryCount={}, 원인={}",
                event.receiverId, event.type, event.retryCount, exceptionMessage
            )

            if (event.retryCount < KafkaConstants.MAX_DLT_RETRY) {
                try {
                    kafkaMessagePublisher.publish(KafkaConstants.NOTIFICATION_TOPIC, objectMapper.writeValueAsString(event.withRetry())).get(5, java.util.concurrent.TimeUnit.SECONDS)
                    log.info("notification 토픽 재투입 - retryCount={}", event.retryCount + 1)
                } catch (e: Exception) {
                    // 재투입 자체가 실패하면 이벤트를 잃어버리지 않도록 영구 저장한다.
                    log.error("notification 토픽 재투입 실패 - DB 영구 저장. receiverId={}", event.receiverId, e)
                    failedNotificationRepository.save(FailedNotification.from(event, "재투입 실패: ${e.message}"))
                }
            } else {
                failedNotificationRepository.save(FailedNotification.from(event, exceptionMessage ?: "unknown"))
                log.error("최대 재시도 초과 - DB 영구 저장. receiverId={}", event.receiverId)
            }
        } catch (e: Exception) {
            log.error("DLT 페이로드 파싱 실패 - payload: {}", payload, e)
        } finally {
            ack.acknowledge()
        }
    }
}
