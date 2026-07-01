package com.study.platform.domain.notification.application

import com.study.platform.domain.apply.event.ApplyApprovedEvent
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
class ApplyApprovedHandler(
    private val kafkaProducer: NotificationKafkaProducer,
    private val outboxEventService: OutboxEventService,
    private val objectMapper: ObjectMapper
) : NotificationHandler<ApplyApprovedEvent> {

    @Async("notificationExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    override fun handle(event: ApplyApprovedEvent) {
        val message = "${event.postTitle} 스터디 지원이 승인되었습니다."
        val outboxEventId = outboxEventService.saveWithIdEmbedded(KafkaConstants.NOTIFICATION_TOPIC, event.applicantId.toString()) { id ->
            objectMapper.writeValueAsString(NotificationEvent(event.applicantId, NotificationType.APPLY_APPROVED, message, event.postId, 0, id))
        }
        kafkaProducer.send(outboxEventId, event.applicantId, NotificationType.APPLY_APPROVED, message, event.postId)
    }
}
