package com.study.platform.domain.notification.application;

import com.study.platform.domain.comment.event.MentionEvent;
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
public class MentionHandler implements NotificationHandler<MentionEvent> {

    private final NotificationService notificationService;
    private final UserRepository userRepository;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Override
    public void handle(MentionEvent event) {
        User receiver = userRepository.findById(event.mentionedUserId())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        String message = event.commenterNickname() + "님이 댓글에서 회원님을 멘션했습니다.";
        notificationService.send(receiver, NotificationType.MENTION, message, event.postId());
    }
}
