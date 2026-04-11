package com.study.platform.domain.notification.application;

import com.study.platform.domain.notification.dto.event.NotificationEvent;
import com.study.platform.domain.notification.model.NotificationType;
import com.study.platform.global.constant.KafkaConstants;
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

    public void send(UUID receiverId, NotificationType type, String message, UUID targetId) {
        NotificationEvent event = new NotificationEvent(receiverId, type, message, targetId);
        String payload = objectMapper.writeValueAsString(event);
        kafkaTemplate.send(KafkaConstants.NOTIFICATION_TOPIC, payload);
    }
}
