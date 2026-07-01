package com.study.platform.domain.notification.application

import com.study.platform.domain.apply.event.ApplyReceivedEvent
import com.study.platform.domain.notification.model.NotificationEvent
import com.study.platform.domain.notification.model.NotificationType
import com.study.platform.global.constant.KafkaConstants
import com.study.platform.global.outbox.application.OutboxEventService
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener
import tools.jackson.databind.ObjectMapper

@Component
class ApplyReceivedHandler(
    private val kafkaProducer: NotificationKafkaProducer,
    private val outboxEventService: OutboxEventService,
    private val objectMapper: ObjectMapper
) : NotificationHandler<ApplyReceivedEvent> {

    @Async("notificationExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    override fun handle(event: ApplyReceivedEvent) {
        val message = "${event.postTitle} 게시글에 새로운 지원서가 도착했습니다."
        val outboxEventId = outboxEventService.saveWithIdEmbedded(KafkaConstants.NOTIFICATION_TOPIC, event.authorId.toString()) { id ->
            objectMapper.writeValueAsString(NotificationEvent(event.authorId, NotificationType.APPLY_RECEIVED, message, event.postId, 0, id))
        }
        kafkaProducer.send(outboxEventId, event.authorId, NotificationType.APPLY_RECEIVED, message, event.postId)
    }
}
