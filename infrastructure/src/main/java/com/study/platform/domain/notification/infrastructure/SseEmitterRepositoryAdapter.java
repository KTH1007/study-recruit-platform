package com.study.platform.domain.notification.infrastructure;

import com.study.platform.domain.notification.model.SseConnection;
import com.study.platform.domain.notification.model.SseEmitterPort;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class SseEmitterRepositoryAdapter implements SseEmitterPort {

    private final Map<UUID, SseConnection> connections = new ConcurrentHashMap<>();

    @Override
    public void save(UUID userId, SseConnection connection) {
        connections.put(userId, connection);
    }

    @Override
    public void delete(UUID userId) {
        connections.remove(userId);
    }

    @Override
    public SseConnection findByUserId(UUID userId) {
        return connections.get(userId);
    }
}
