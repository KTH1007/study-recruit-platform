package com.study.platform.global.outbox.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {

    List<OutboxEvent> findAllByStatusAndCreatedAtBefore(OutboxEventStatus status, LocalDateTime createdAt);

    @Modifying
    @Query("UPDATE OutboxEvent o " +
            "SET o.status = 'SENT', o.sentAt = NOW() " +
            "WHERE o.messageKey = :messageKey AND o.topic = :topic AND o.status = 'PENDING'")
    int markSentByMessageKeyAndTopic(@Param("messageKey") String messageKey, @Param("topic") String topic);
}
