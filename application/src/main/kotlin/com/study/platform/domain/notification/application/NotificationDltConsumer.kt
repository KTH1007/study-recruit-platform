package com.study.platform.domain.notification.application

import com.study.platform.domain.notification.event.NotificationEvent
import com.study.platform.domain.notification.model.FailedNotification
import com.study.platform.domain.notification.model.FailedNotificationRepository
import com.study.platform.global.constant.KafkaConstants
import com.study.platform.global.kafka.DltRetryHandler
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
    objectMapper: ObjectMapper,
    kafkaMessagePublisher: KafkaMessagePublisher,
    failedNotificationRepository: FailedNotificationRepository
) {
    private val handler = DltRetryHandler(
        objectMapper = objectMapper,
        kafkaMessagePublisher = kafkaMessagePublisher,
        log = LoggerFactory.getLogger(NotificationDltConsumer::class.java)!!,
        eventClass = NotificationEvent::class.java,
        retryTopic = KafkaConstants.NOTIFICATION_TOPIC,
        maxRetry = KafkaConstants.MAX_DLT_RETRY,
        getRetryCount = { it.retryCount },
        withIncrementedRetry = { it.withRetry() },
        describe = { "receiverId=${it.receiverId}, type=${it.type}" },
        onPermanentFailure = { event, reason -> failedNotificationRepository.save(FailedNotification.from(event, reason)) }
    )

    @Transactional
    @KafkaListener(topics = [KafkaConstants.NOTIFICATION_DLT_TOPIC], groupId = KafkaConstants.NOTIFICATION_DLT_GROUP)
    fun consume(
        payload: String,
        ack: Acknowledgment,
        @Header(name = KafkaHeaders.EXCEPTION_MESSAGE, required = false) exceptionMessage: String?
    ) = handler.handle(payload, ack, exceptionMessage)
}
