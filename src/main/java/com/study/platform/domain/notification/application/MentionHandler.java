package com.study.platform.domain.notification.application;

import com.study.platform.domain.comment.event.MentionEvent;
import com.study.platform.domain.notification.model.NotificationType;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class MentionHandler implements NotificationHandler<MentionEvent> {

    private final NotificationKafkaProducer kafkaProducer;

    @Async("notificationExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Override
    public void handle(MentionEvent event) {
        String message = event.commenterNickname() + "님이 댓글에서 회원님을 멘션했습니다.";
        kafkaProducer.send(event.mentionedUserId(), NotificationType.MENTION, message, event.postId());
    }
}
