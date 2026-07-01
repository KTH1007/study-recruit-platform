package com.study.platform.domain.notification.application

import com.study.platform.domain.notification.model.NotificationEvent
import com.study.platform.domain.notification.model.NotificationType
import com.study.platform.domain.post.event.PostDeadlineReminderEvent
import com.study.platform.global.constant.KafkaConstants
import com.study.platform.global.outbox.application.OutboxEventService
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener
import tools.jackson.databind.ObjectMapper

@Component
class PostDeadlineHandler(
    private val kafkaProducer: NotificationKafkaProducer,
    private val outboxEventService: OutboxEventService,
    private val objectMapper: ObjectMapper
) : NotificationHandler<PostDeadlineReminderEvent> {

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    override fun handle(event: PostDeadlineReminderEvent) {
        val message = "${event.postTitle} 게시글 모집 마감이 내일입니다."
        val outboxEventId = outboxEventService.saveWithIdEmbedded(KafkaConstants.NOTIFICATION_TOPIC, event.authorId.toString()) { id ->
            objectMapper.writeValueAsString(NotificationEvent(event.authorId, NotificationType.POST_DEADLINE, message, event.postId, 0, id))
        }
        kafkaProducer.send(outboxEventId, event.authorId, NotificationType.POST_DEADLINE, message, event.postId)
    }
}
