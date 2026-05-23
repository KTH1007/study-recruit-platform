package com.study.platform.domain.notification.application;

import com.study.platform.domain.notification.dto.event.NotificationEvent;
import com.study.platform.domain.notification.model.NotificationType;
import com.study.platform.domain.post.event.PostDeadlineReminderEvent;
import com.study.platform.global.constant.KafkaConstants;
import com.study.platform.global.outbox.application.OutboxEventService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
public class PostDeadlineHandler implements NotificationHandler<PostDeadlineReminderEvent> {

    private final NotificationKafkaProducer kafkaProducer;
    private final OutboxEventService outboxEventService;
    private final ObjectMapper objectMapper;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Override
    public void handle(PostDeadlineReminderEvent event) {
        String message = event.postTitle() + " 게시글 모집 마감이 내일입니다.";
        String payload = objectMapper.writeValueAsString(
                new NotificationEvent(event.authorId(), NotificationType.POST_DEADLINE, message, event.postId(), 0));
        outboxEventService.saveWithNewTx(KafkaConstants.NOTIFICATION_TOPIC, event.authorId().toString(), payload);
        kafkaProducer.send(event.authorId(), NotificationType.POST_DEADLINE, message, event.postId());
    }
}