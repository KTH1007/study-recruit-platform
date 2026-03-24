package com.study.platform.domain.notification.application;

import com.study.platform.domain.comment.event.CommentCreatedEvent;
import com.study.platform.domain.notification.model.NotificationType;
import com.study.platform.domain.user.model.User;
import com.study.platform.domain.user.model.UserRepository;
import com.study.platform.global.exception.CustomException;
import com.study.platform.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class CommentCreatedHandler implements NotificationHandler<CommentCreatedEvent> {

    private final NotificationService notificationService;
    private final UserRepository userRepository;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Override
    public void handle(CommentCreatedEvent event) {
        User receiver = userRepository.findById(event.authorId())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        String message = event.postTitle() + " 게시글에 댓글이 달렸습니다.";
        notificationService.send(receiver, NotificationType.COMMENT_CREATED, message, event.postId());
    }
}
