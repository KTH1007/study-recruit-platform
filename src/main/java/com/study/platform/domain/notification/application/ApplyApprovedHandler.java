package com.study.platform.domain.notification.application;

import com.study.platform.domain.apply.event.ApplyApprovedEvent;
import com.study.platform.domain.notification.model.NotificationType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class ApplyApprovedHandler implements NotificationHandler<ApplyApprovedEvent> {

    private final NotificationService notificationService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Override
    public void handle(ApplyApprovedEvent event) {
        String message = event.postTitle() + " 스터디 지원이 승인되었습니다.";
        notificationService.send(event.applicantId(), NotificationType.APPLY_APPROVED, message, event.postId());
    }
}