package com.study.platform.domain.notification.application;

import com.study.platform.domain.notification.dto.event.NotificationEvent;
import com.study.platform.domain.notification.dto.response.NotificationResponse;
import com.study.platform.domain.notification.model.FailedNotification;
import com.study.platform.domain.notification.model.FailedNotificationRepository;
import com.study.platform.domain.notification.model.Notification;
import com.study.platform.domain.notification.model.NotificationRepository;
import com.study.platform.domain.user.model.User;
import com.study.platform.domain.user.model.UserRepository;
import com.study.platform.global.constant.KafkaConstants;
import com.study.platform.global.exception.CustomException;
import com.study.platform.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationKafkaConsumer {

    private static final String NOTIFICATION_CHANNEL = "notification";

    private final ObjectMapper objectMapper;
    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;
    private final StringRedisTemplate stringRedisTemplate;
    private final FailedNotificationRepository failedNotificationRepository;

    @Transactional
    @KafkaListener(topics = KafkaConstants.NOTIFICATION_TOPIC, groupId = KafkaConstants.NOTIFICATION_GROUP)
    public void consume(String payload, Acknowledgment ack) {
        NotificationEvent event = objectMapper.readValue(payload, NotificationEvent.class);

        if (event.retryCount() >= KafkaConstants.MAX_DLT_RETRY) {
            failedNotificationRepository.save(FailedNotification.from(event, "메인 Consumer 최종 실패"));
            ack.acknowledge();
            return;
        }
        User receiver = userRepository.findById(event.receiverId())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        Notification notification = Notification.create(receiver, event.type(), event.message(), event.targetId());
        notificationRepository.save(notification);
        ack.acknowledge(); // DB 저장 완료 후 커밋
        stringRedisTemplate.convertAndSend(NOTIFICATION_CHANNEL, objectMapper.writeValueAsString(NotificationResponse.from(notification)));
    }
}
