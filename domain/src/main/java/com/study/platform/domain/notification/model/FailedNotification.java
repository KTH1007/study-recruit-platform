package com.study.platform.domain.notification.model;

import com.study.platform.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "failed_notifications")
public class FailedNotification extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(nullable = false, columnDefinition = "BINARY(16)")
    private UUID receiverId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private NotificationType type;

    @Column(nullable = false)
    private String message;

    @Column(columnDefinition = "BINARY(16)")
    private UUID targetId;

    @Column(columnDefinition = "text")
    private String failureReason;

    public static FailedNotification from(NotificationEvent event, String failureReason) {
        FailedNotification entity = new FailedNotification();
        entity.receiverId = event.receiverId();
        entity.type = event.type();
        entity.message = event.message();
        entity.targetId = event.targetId();
        entity.failureReason = failureReason;
        return entity;
    }
}
