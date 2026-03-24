package com.study.platform.domain.notification.application;

import com.study.platform.domain.notification.model.NotificationType;
import com.study.platform.domain.post.event.PostDeadlineReminderEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class PostDeadlineHandler implements NotificationHandler<PostDeadlineReminderEvent> {

    private final NotificationService notificationService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Override
    public void handle(PostDeadlineReminderEvent event) {
        String message = event.postTitle() + " 게시글 모집 마감이 내일입니다.";
        notificationService.send(event.authorId(), NotificationType.POST_DEADLINE, message, event.postId());
    }
}