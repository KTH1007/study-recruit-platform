package com.study.platform.domain.notification.model;

import com.study.platform.domain.user.model.User;
import com.study.platform.global.entity.BaseTimeEntity;
import com.study.platform.global.exception.CustomException;
import com.study.platform.global.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
        name = "notifications",
        indexes = {
                @Index(name = "idx_notification_receiver_read_created", columnList = "receiver_id, is_read, created_at"),
                @Index(name = "idx_notification_receiver_created", columnList = "receiver_id, created_at")
        }
)
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

    // 알림 클릭 시 이동할 대상 ID (게시글, 일정 등)
    @Column(columnDefinition = "BINARY(16)")
    private UUID targetId;

    @Column(nullable = false)
    private boolean isRead = false;

    @Builder
    private Notification(User receiver, NotificationType type, String message, UUID targetId) {
        this.receiver = receiver;
        this.type = type;
        this.message = message;
        this.targetId = targetId;
    }

    public static Notification create(User receiver, NotificationType type, String message, UUID targetId) {
        return Notification.builder()
                .receiver(receiver)
                .type(type)
                .message(message)
                .targetId(targetId)
                .build();
    }

    public void markAsRead() {
        this.isRead = true;
    }

    public void validateReceiver(UUID userId) {
        if (!this.receiver.getId().equals(userId)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }
    }
}
