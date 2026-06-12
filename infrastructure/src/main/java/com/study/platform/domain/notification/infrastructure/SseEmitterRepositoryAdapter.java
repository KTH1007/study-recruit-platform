package com.study.platform.domain.notification.infrastructure;

import com.study.platform.domain.notification.model.SseEmitterPort;
import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class SseEmitterRepositoryAdapter implements SseEmitterPort {

    private final Map<UUID, SseEmitter> emitters = new ConcurrentHashMap<>();

    @Override
    public void save(UUID userId, SseEmitter emitter) {
        emitters.put(userId, emitter);
    }

    @Override
    public void delete(UUID userId) {
        emitters.remove(userId);
    }

    @Override
    public SseEmitter findByUserId(UUID userId) {
        return emitters.get(userId);
    }
}
