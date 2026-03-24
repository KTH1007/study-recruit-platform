package com.study.platform.domain.notification.application;

import com.study.platform.domain.apply.event.ApplyRejectedEvent;
import com.study.platform.domain.notification.model.NotificationType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class ApplyRejectedHandler implements NotificationHandler<ApplyRejectedEvent> {

    private final NotificationService notificationService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Override
    public void handle(ApplyRejectedEvent event) {
        String message = event.postTitle() + " 스터디 지원이 거절되었습니다.";
        notificationService.send(event.applicantId(), NotificationType.APPLY_REJECTED, message, event.postId());
    }
}
