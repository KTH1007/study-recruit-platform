package com.study.platform.domain.notification.application;

import com.study.platform.domain.apply.event.ApplyRejectedEvent;
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
public class ApplyRejectedHandler implements NotificationHandler<ApplyRejectedEvent> {

    private final NotificationKafkaProducer kafkaProducer;
    private final OutboxEventService outboxEventService;
    private final ObjectMapper objectMapper;

    @Async("notificationExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Override
    public void handle(ApplyRejectedEvent event) {
        String message = event.postTitle() + " 스터디 지원이 거절되었습니다.";
        String payload = objectMapper.writeValueAsString(
                new NotificationEvent(event.applicantId(), NotificationType.APPLY_REJECTED, message, event.postId(), 0));
        Long outboxEventId = outboxEventService.saveWithNewTx(KafkaConstants.NOTIFICATION_TOPIC, event.applicantId().toString(), payload);
        kafkaProducer.send(outboxEventId, event.applicantId(), NotificationType.APPLY_REJECTED, message, event.postId());
    }
}
