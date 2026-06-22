package com.study.platform.domain.notification.model

import java.util.UUID

interface SseEmitterPort {
    fun save(userId: UUID, connection: SseConnection)
    fun delete(userId: UUID)
    fun findByUserId(userId: UUID): SseConnection?
}
