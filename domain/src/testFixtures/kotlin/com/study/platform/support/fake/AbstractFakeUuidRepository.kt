package com.study.platform.support.fake

import org.springframework.test.util.ReflectionTestUtils
import java.time.LocalDateTime
import java.util.UUID

abstract class AbstractFakeUuidRepository<T : Any> {

    protected val store: MutableMap<UUID, T> = LinkedHashMap()

    protected abstract fun idOf(entity: T): UUID?

    protected open fun hasTimestamps(): Boolean = false

    protected fun saveEntity(entity: T): T {
        val id = idOf(entity) ?: UUID.randomUUID().also { ReflectionTestUtils.setField(entity, "id", it) }
        if (hasTimestamps()) {
            if (ReflectionTestUtils.getField(entity, "createdAt") == null) {
                ReflectionTestUtils.setField(entity, "createdAt", LocalDateTime.now())
            }
            ReflectionTestUtils.setField(entity, "updatedAt", LocalDateTime.now())
        }
        store[id] = entity
        return entity
    }

    fun findById(id: UUID): T? = store[id]

    protected fun deleteEntity(entity: T) {
        store.remove(idOf(entity))
    }
}
