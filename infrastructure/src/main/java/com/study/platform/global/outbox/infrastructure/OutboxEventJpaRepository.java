package com.study.platform.global.outbox.infrastructure;

import com.study.platform.global.outbox.model.OutboxEvent;
import com.study.platform.global.outbox.model.OutboxEventStatus;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface OutboxEventJpaRepository extends JpaRepository<OutboxEvent, Long> {

    List<OutboxEvent> findAllByStatusAndCreatedAtBefore(OutboxEventStatus status, LocalDateTime createdAt);

    @Modifying
    @Query("UPDATE OutboxEvent o SET o.status = 'SENT', o.sentAt = NOW() " +
            "WHERE o.id = :id AND o.status = 'PENDING'")
    int markSentById(@Param("id") Long id);

    @Modifying
    @Query("UPDATE OutboxEvent o SET o.status = 'FAILED_PERMANENTLY' " +
            "WHERE o.id = :id")
    void markFailedPermanently(@Param("id") Long id);
}
