package com.study.platform.domain.notification.application;

import tools.jackson.databind.ObjectMapper;
import com.study.platform.domain.notification.dto.response.NotificationResponse;
import com.study.platform.domain.notification.infrastructure.SseEmitterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisNotificationSubscriber {

    private static final String SSE_EVENT_NAME = "notification";

    private final SseEmitterRepository sseEmitterRepository;
    private final ObjectMapper objectMapper;

    public void onMessage(String message, String channel) {
        try {
            NotificationResponse response = objectMapper.readValue(message, NotificationResponse.class);
            SseEmitter emitter = sseEmitterRepository.findByUserId(response.receiverId());
            if (emitter == null) {
                return;
            }
            emitter.send(SseEmitter.event()
                    .name(SSE_EVENT_NAME)
                    .data(response));
        } catch (IOException e) {
            log.warn("SSE 전송 실패 : {}", e.getMessage());
        }
    }
}
