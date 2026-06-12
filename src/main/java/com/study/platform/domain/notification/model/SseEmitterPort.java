package com.study.platform.domain.notification.model;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.UUID;

public interface SseEmitterPort {

    void save(UUID userId, SseEmitter emitter);

    void delete(UUID userId);

    SseEmitter findByUserId(UUID userId);
}
