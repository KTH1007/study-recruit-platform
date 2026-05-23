package com.study.platform.domain.notification.application;

import com.study.platform.domain.apply.event.ApplyReceivedEvent;
import com.study.platform.domain.notification.dto.event.NotificationEvent;
import com.study.platform.domain.notification.model.NotificationType;
import com.study.platform.global.constant.KafkaConstants;
import com.study.platform.global.outbox.application.OutboxEventService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
public class ApplyReceivedHandler implements NotificationHandler<ApplyReceivedEvent> {

    private final NotificationKafkaProducer kafkaProducer;
    private final OutboxEventService outboxEventService;
    private final ObjectMapper objectMapper;

    @Async("notificationExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void handle(ApplyReceivedEvent event) {
        String message = event.postTitle() + " 게시글에 새로운 지원서가 도착했습니다.";
        String payload = objectMapper.writeValueAsString(
                new NotificationEvent(event.authorId(), NotificationType.APPLY_RECEIVED, message, event.postId(), 0));
        outboxEventService.save(KafkaConstants.NOTIFICATION_TOPIC, event.authorId().toString(), payload);
        kafkaProducer.send(event.authorId(), NotificationType.APPLY_RECEIVED, message, event.postId());
    }
}
