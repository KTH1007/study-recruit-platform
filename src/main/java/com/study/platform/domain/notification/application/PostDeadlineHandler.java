package com.study.platform.domain.notification.application;

import com.study.platform.domain.notification.model.NotificationType;
import com.study.platform.domain.post.event.PostDeadlineReminderEvent;
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
public class PostDeadlineHandler implements NotificationHandler<PostDeadlineReminderEvent> {

    private final NotificationService notificationService;
    private final UserRepository userRepository;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Override
    public void handle(PostDeadlineReminderEvent event) {
        User receiver = userRepository.findById(event.authorId())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        String message = event.postTitle() + " 게시글 모집 마감이 내일입니다.";
        notificationService.send(receiver, NotificationType.POST_DEADLINE, message, event.postId());
    }
}