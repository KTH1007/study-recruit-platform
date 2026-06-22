package com.study.platform.domain.notification.application

import com.study.platform.domain.comment.event.CommentCreatedEvent
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
class CommentCreatedHandler(
    private val kafkaProducer: NotificationKafkaProducer,
    private val outboxEventService: OutboxEventService,
    private val objectMapper: ObjectMapper
) : NotificationHandler<CommentCreatedEvent> {

    @Async("notificationExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    override fun handle(event: CommentCreatedEvent) {
        val message = "${event.postTitle} 게시글에 댓글이 달렸습니다."
        val payload = objectMapper.writeValueAsString(
            NotificationEvent(event.authorId, NotificationType.COMMENT_CREATED, message, event.postId, 0)
        )
        val outboxEventId = outboxEventService.saveWithNewTx(KafkaConstants.NOTIFICATION_TOPIC, event.authorId.toString(), payload)
        kafkaProducer.send(outboxEventId, event.authorId, NotificationType.COMMENT_CREATED, message, event.postId)
    }
}
