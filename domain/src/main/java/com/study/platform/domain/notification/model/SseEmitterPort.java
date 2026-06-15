package com.study.platform.domain.notification.model;

import java.util.UUID;

public interface SseEmitterPort {

    void save(UUID userId, SseConnection connection);

    void delete(UUID userId);

    SseConnection findByUserId(UUID userId);
}
