package com.study.platform.domain.notification.application;

import com.study.platform.domain.notification.dto.event.NotificationEvent;
import com.study.platform.domain.notification.model.NotificationType;
import com.study.platform.global.constant.KafkaConstants;
import com.study.platform.global.outbox.application.OutboxEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationKafkaProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final OutboxEventService outboxEventService;

    public void send(UUID receiverId, NotificationType type, String message, UUID targetId) {
        NotificationEvent event = new NotificationEvent(receiverId, type, message, targetId, 0);
        String payload = objectMapper.writeValueAsString(event);
        kafkaTemplate.send(KafkaConstants.NOTIFICATION_TOPIC, receiverId.toString(), payload)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        outboxEventService.markSent(receiverId.toString(), KafkaConstants.NOTIFICATION_TOPIC);
                        log.info("알림 이벤트 발행 성공 - receiverId: {}, type: {}", receiverId, type);
                    } else {
                        log.warn("알림 이벤트 발행 실패 - outbox 스케줄러가 재시도 예정. receiverId: {}, type: {}", receiverId, type, ex);
                    }
                });
    }
}
