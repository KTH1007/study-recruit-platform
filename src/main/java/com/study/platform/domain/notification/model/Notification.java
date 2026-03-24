package com.study.platform.domain.notification.model;

import com.study.platform.domain.user.model.User;
import com.study.platform.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "notifications")
public class Notification extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id", nullable = false)
    private User receiver;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private NotificationType type;

    @Column(nullable = false)
    private String message;

    // 알림 클릭 시 이동할 게시글 ID (null인 경우 시스템 알림)
    @Column(columnDefinition = "BINARY(16)")
    private UUID postId;

    @Column(nullable = false)
    private boolean isRead = false;

    @Builder
    private Notification(User receiver, NotificationType type, String message, UUID postId) {
        this.receiver = receiver;
        this.type = type;
        this.message = message;
        this.postId = postId;
    }

    public static Notification create(User receiver, NotificationType type, String message, UUID postId) {
        return Notification.builder()
                .receiver(receiver)
                .type(type)
                .message(message)
                .postId(postId)
                .build();
    }

    public void markAsRead() {
        this.isRead = true;
    }
}
