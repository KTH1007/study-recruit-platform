package com.study.platform.domain.notification.application

import com.study.platform.domain.comment.event.MentionEvent
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
class MentionHandler(
    private val kafkaProducer: NotificationKafkaProducer,
    private val outboxEventService: OutboxEventService,
    private val objectMapper: ObjectMapper
) : NotificationHandler<MentionEvent> {

    @Async("notificationExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    override fun handle(event: MentionEvent) {
        val message = "${event.commenterNickname}님이 댓글에서 회원님을 멘션했습니다."
        val outboxEventId = outboxEventService.saveWithIdEmbedded(KafkaConstants.NOTIFICATION_TOPIC, event.mentionedUserId.toString()) { id ->
            objectMapper.writeValueAsString(NotificationEvent(event.mentionedUserId, NotificationType.MENTION, message, event.postId, 0, id))
        }
        kafkaProducer.send(outboxEventId, event.mentionedUserId, NotificationType.MENTION, message, event.postId)
    }
}
