package com.study.platform.domain.notification.application;

import com.study.platform.domain.notification.model.NotificationEvent;
import com.study.platform.domain.notification.model.NotificationType;
import com.study.platform.global.constant.KafkaConstants;
import com.study.platform.global.kafka.KafkaMessagePublisher;
import com.study.platform.global.outbox.application.OutboxEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationKafkaProducer implements com.study.platform.domain.notification.model.NotificationPublisher {

    private final KafkaMessagePublisher kafkaMessagePublisher;
    private final ObjectMapper objectMapper;
    private final OutboxEventService outboxEventService;

    public void send(Long outboxEventId, UUID receiverId, NotificationType type, String message, UUID targetId) {
        NotificationEvent event = new NotificationEvent(receiverId, type, message, targetId, 0);
        String payload = objectMapper.writeValueAsString(event);
        kafkaMessagePublisher.publish(KafkaConstants.NOTIFICATION_TOPIC, receiverId.toString(), payload)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        outboxEventService.markSent(outboxEventId);
                        log.info("알림 이벤트 발행 성공 - receiverId: {}, type: {}", receiverId, type);
                    } else {
                        log.warn("알림 이벤트 발행 실패 - outbox 스케줄러가 재시도 예정. receiverId: {}, type: {}", receiverId, type, ex);
                    }
                });
    }
}
