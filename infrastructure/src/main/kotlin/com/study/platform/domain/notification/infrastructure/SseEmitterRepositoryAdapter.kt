package com.study.platform.domain.notification.infrastructure

import com.study.platform.domain.notification.model.SseConnection
import com.study.platform.domain.notification.model.SseEmitterPort
import org.springframework.stereotype.Repository
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

@Repository
class SseEmitterRepositoryAdapter : SseEmitterPort {

    private val connections: MutableMap<UUID, MutableMap<String, SseConnection>> = ConcurrentHashMap()

    override fun save(userId: UUID, connectionId: String, connection: SseConnection) {
        connections.computeIfAbsent(userId) { ConcurrentHashMap() }[connectionId] = connection
    }

    override fun delete(userId: UUID, connectionId: String) {
        connections[userId]?.let { byConnectionId ->
            byConnectionId.remove(connectionId)
            if (byConnectionId.isEmpty()) {
                connections.remove(userId)
            }
        }
    }

    override fun findAllByUserId(userId: UUID): List<SseConnection> =
        connections[userId]?.values?.toList() ?: emptyList()
}
