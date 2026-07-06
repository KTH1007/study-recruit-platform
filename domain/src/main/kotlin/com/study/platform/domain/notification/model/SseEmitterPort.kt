package com.study.platform.domain.notification.model

import java.util.UUID

interface SseEmitterPort {
    fun save(userId: UUID, connectionId: String, connection: SseConnection)
    fun delete(userId: UUID, connectionId: String)
    fun findAllByUserId(userId: UUID): List<SseConnection>
}
