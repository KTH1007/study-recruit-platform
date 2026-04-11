package com.study.platform.domain.notification.application;

import com.study.platform.domain.apply.event.ApplyRejectedEvent;
import com.study.platform.domain.notification.model.NotificationType;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class ApplyRejectedHandler implements NotificationHandler<ApplyRejectedEvent> {

    private final NotificationKafkaProducer kafkaProducer;

    @Async("notificationExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Override
    public void handle(ApplyRejectedEvent event) {
        String message = event.postTitle() + " 스터디 지원이 거절되었습니다.";
        kafkaProducer.send(event.applicantId(), NotificationType.APPLY_REJECTED, message, event.postId());
    }
}