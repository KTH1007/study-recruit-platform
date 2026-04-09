package com.study.platform.domain.notification.application;

import com.study.platform.domain.apply.event.ApplyReceivedEvent;
import com.study.platform.domain.notification.model.NotificationType;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class ApplyReceivedHandler implements NotificationHandler<ApplyReceivedEvent> {

    private final NotificationService notificationService;

    @Async("notificationExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Override
    public void handle(ApplyReceivedEvent event) {
        String message = event.postTitle() + " 게시글에 새로운 지원서가 도착했습니다.";
        notificationService.send(event.authorId(), NotificationType.APPLY_RECEIVED, message, event.postId());
    }
}