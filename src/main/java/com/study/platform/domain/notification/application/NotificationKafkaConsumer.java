package com.study.platform.domain.notification.application;

import com.study.platform.domain.notification.dto.event.NotificationEvent;
import com.study.platform.domain.notification.dto.response.NotificationResponse;
import com.study.platform.domain.notification.infrastructure.SseEmitterRepository;
import com.study.platform.domain.notification.model.Notification;
import com.study.platform.domain.notification.model.NotificationRepository;
import com.study.platform.domain.user.model.User;
import com.study.platform.domain.user.model.UserRepository;
import com.study.platform.global.constant.KafkaConstants;
import com.study.platform.global.exception.CustomException;
import com.study.platform.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationKafkaConsumer {

    private static final String SSE_EVENT_NAME = "notification";

    private final ObjectMapper objectMapper;
    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;
    private final SseEmitterRepository sseEmitterRepository;

    @Transactional
    @KafkaListener(topics = KafkaConstants.NOTIFICATION_TOPIC, groupId = KafkaConstants.NOTIFICATION_GROUP)
    public void consume(String payload, Acknowledgment ack) {
        NotificationEvent event = objectMapper.readValue(payload, NotificationEvent.class);
        User receiver = userRepository.findById(event.receiverId())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        Notification notification = Notification.create(receiver, event.type(), event.message(), event.targetId());
        notificationRepository.save(notification);
        ack.acknowledge(); // DB 저장 완료 후 커밋
        sendSse(NotificationResponse.from(notification));
    }

    private void sendSse(NotificationResponse response) {
        SseEmitter emitter = sseEmitterRepository.findByUserId(response.receiverId());
        if (emitter == null) {
            return;
        }
        try {
            emitter.send(SseEmitter.event()
                    .name(SSE_EVENT_NAME)
                    .data(response));
        } catch (IOException e) {
            log.warn("SSE 전송 실패 : {}", e.getMessage());
        }
    }
}
