package com.study.platform.global.outbox.infrastructure

import com.study.platform.global.outbox.model.OutboxEvent
import com.study.platform.global.outbox.model.OutboxEventStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDateTime

interface OutboxEventJpaRepository : JpaRepository<OutboxEvent, Long> {

    fun findAllByStatusAndCreatedAtBefore(status: OutboxEventStatus, createdAt: LocalDateTime): List<OutboxEvent>

    @Modifying
    @Query("UPDATE OutboxEvent o SET o.status = 'SENT', o.sentAt = NOW() WHERE o.id = :id AND o.status = 'PENDING'")
    fun markSentById(@Param("id") id: Long): Int

    @Modifying
    @Query("UPDATE OutboxEvent o SET o.status = 'FAILED_PERMANENTLY' WHERE o.id = :id")
    fun markFailedPermanently(@Param("id") id: Long)
}
