package com.study.platform.domain.notification.application;

import com.study.platform.domain.comment.event.CommentCreatedEvent;
import com.study.platform.domain.notification.model.NotificationType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class CommentCreatedHandler implements NotificationHandler<CommentCreatedEvent> {

    private final NotificationService notificationService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Override
    public void handle(CommentCreatedEvent event) {
        String message = event.postTitle() + " 게시글에 댓글이 달렸습니다.";
        notificationService.send(event.authorId(), NotificationType.COMMENT_CREATED, message, event.postId());
    }
}
