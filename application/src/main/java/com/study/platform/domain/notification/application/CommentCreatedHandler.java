package com.study.platform.domain.notification.application;

import com.study.platform.domain.comment.event.CommentCreatedEvent;
import com.study.platform.domain.notification.model.NotificationEvent;
import com.study.platform.domain.notification.model.NotificationType;
import com.study.platform.global.constant.KafkaConstants;
import com.study.platform.global.outbox.application.OutboxEventService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
public class CommentCreatedHandler implements NotificationHandler<CommentCreatedEvent> {

    private final NotificationKafkaProducer kafkaProducer;
    private final OutboxEventService outboxEventService;
    private final ObjectMapper objectMapper;

    @Async("notificationExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Override
    public void handle(CommentCreatedEvent event) {
        String message = event.postTitle() + " 게시글에 댓글이 달렸습니다.";
        String payload = objectMapper.writeValueAsString(
                new NotificationEvent(event.authorId(), NotificationType.COMMENT_CREATED, message, event.postId(), 0));
        Long outboxEventId = outboxEventService.saveWithNewTx(KafkaConstants.NOTIFICATION_TOPIC, event.authorId().toString(), payload);
        kafkaProducer.send(outboxEventId, event.authorId(), NotificationType.COMMENT_CREATED, message, event.postId());
    }
}
