package com.study.platform.domain.notification.application;

import com.study.platform.domain.apply.event.ApplyApprovedEvent;
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
public class ApplyApprovedHandler implements NotificationHandler<ApplyApprovedEvent> {

    private final NotificationService notificationService;
    private final UserRepository userRepository;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Override
    public void handle(ApplyApprovedEvent event) {
        User receiver = userRepository.findById(event.applicantId())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        String message = event.postTitle() + " 스터디 지원이 승인되었습니다.";
        notificationService.send(receiver, NotificationType.APPLY_APPROVED, message, event.postId());
    }
}