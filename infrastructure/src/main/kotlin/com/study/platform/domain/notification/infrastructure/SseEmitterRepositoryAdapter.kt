package com.study.platform.domain.notification.infrastructure

import com.study.platform.domain.notification.model.SseConnection
import com.study.platform.domain.notification.model.SseEmitterPort
import org.springframework.stereotype.Repository
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

@Repository
class SseEmitterRepositoryAdapter : SseEmitterPort {

    private val connections: MutableMap<UUID, SseConnection> = ConcurrentHashMap()

    override fun save(userId: UUID, connection: SseConnection) {
        connections[userId] = connection
    }

    override fun delete(userId: UUID) {
        connections.remove(userId)
    }

    override fun findByUserId(userId: UUID): SseConnection? =
        connections[userId]
}
