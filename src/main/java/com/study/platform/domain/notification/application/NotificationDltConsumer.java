package com.study.platform.domain.notification.application;

import com.study.platform.domain.notification.dto.event.NotificationEvent;
import com.study.platform.domain.notification.model.FailedNotification;
import com.study.platform.domain.notification.model.FailedNotificationRepository;
import com.study.platform.global.constant.KafkaConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationDltConsumer {

    private final ObjectMapper objectMapper;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final FailedNotificationRepository failedNotificationRepository;

    @KafkaListener(topics = KafkaConstants.NOTIFICATION_DLT_TOPIC, groupId = KafkaConstants.NOTIFICATION_DLT_GROUP)
    public void consume(String payload, Acknowledgment ack,
                        @Header(name = KafkaHeaders.EXCEPTION_MESSAGE, required = false) String exceptionMessage) {
        NotificationEvent event = objectMapper.readValue(payload, NotificationEvent.class);
        log.error("DLT 수신 - receiverId={}, type={}, retryCount={}, 원인={}",
                event.receiverId(), event.type(), event.retryCount(), exceptionMessage);

        if (event.retryCount() < KafkaConstants.MAX_DLT_RETRY) {
            kafkaTemplate.send(KafkaConstants.NOTIFICATION_TOPIC, objectMapper.writeValueAsString(event.withRetry()));
            log.info("notification 토픽 재투입 - retryCount={}", event.retryCount() + 1);
        } else {
            failedNotificationRepository.save(FailedNotification.from(event, exceptionMessage));
            log.error("최대 재시도 초과 - DB 영구 저장. receiverId={}", event.receiverId());
        }

        ack.acknowledge();
    }
}
