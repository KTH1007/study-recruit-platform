package com.study.platform.domain.chat.model;

import com.study.platform.domain.team.model.StudyTeam;
import com.study.platform.domain.user.model.User;
import com.study.platform.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@Entity
@Table(name = "chat_messages")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatMessage extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "BINARY(16)")
    public UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private StudyTeam team;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Builder
    private ChatMessage(StudyTeam team, User sender, String content) {
        this.team = team;
        this.sender = sender;
        this.content = content;
    }

    public static ChatMessage create(StudyTeam team, User sender, String content) {
        return ChatMessage.builder()
                .team(team)
                .sender(sender)
                .content(content)
                .build();
    }
}
