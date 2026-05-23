package com.study.platform.domain.notification.application;

import com.study.platform.domain.comment.event.MentionEvent;
import com.study.platform.domain.notification.dto.event.NotificationEvent;
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
public class MentionHandler implements NotificationHandler<MentionEvent> {

    private final NotificationKafkaProducer kafkaProducer;
    private final OutboxEventService outboxEventService;
    private final ObjectMapper objectMapper;

    @Async("notificationExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Override
    public void handle(MentionEvent event) {
        String message = event.commenterNickname() + "님이 댓글에서 회원님을 멘션했습니다.";
        String payload = objectMapper.writeValueAsString(
                new NotificationEvent(event.mentionedUserId(), NotificationType.MENTION, message, event.postId(), 0));
        outboxEventService.saveWithNewTx(KafkaConstants.NOTIFICATION_TOPIC, event.mentionedUserId().toString(), payload);
        kafkaProducer.send(event.mentionedUserId(), NotificationType.MENTION, message, event.postId());
    }
}
