package com.study.platform.domain.chat.model;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, UUID> {

    @Query("SELECT m FROM ChatMessage m " +
            "JOIN FETCH m.sender JOIN FETCH m.team WHERE m.team.id = :teamId " +
            "ORDER BY m.createdAt DESC")
    Slice<ChatMessage> findByTeamIdWithSender(@Param("teamId") UUID teamId, Pageable pageable);
}
