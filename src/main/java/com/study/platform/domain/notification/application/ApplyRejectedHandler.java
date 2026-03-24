package com.study.platform.domain.notification.application;

import com.study.platform.domain.apply.event.ApplyRejectedEvent;
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
public class ApplyRejectedHandler implements NotificationHandler<ApplyRejectedEvent> {

    private final NotificationService notificationService;
    private final UserRepository userRepository;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Override
    public void handle(ApplyRejectedEvent event) {
        User receiver = userRepository.findById(event.applicantId())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        String message = event.postTitle() + " 스터디 지원이 거절되었습니다.";
        notificationService.send(receiver, NotificationType.APPLY_REJECTED, message, event.postId());
    }
}
