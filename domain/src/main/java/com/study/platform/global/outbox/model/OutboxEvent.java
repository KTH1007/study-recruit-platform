package com.study.platform.global.outbox.model;

import com.study.platform.global.entity.BaseTimeEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "outbox_event")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Schema(description = "Outbox 이벤트")
public class OutboxEvent extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Schema(description = "발행할 Kafka 토픽")
    @Column(nullable = false)
    private String topic;

    @Schema(description = "Kafka 메시지 키")
    @Column(nullable = false)
    private String messageKey;

    @Schema(description = "직렬화된 이벤트 JSON")
    @Column(nullable = false, columnDefinition = "TEXT")
    private String payload;

    @Schema(description = "발행 상태")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OutboxEventStatus status;

    @Schema(description = "재시도 횟수")
    @Column(nullable = false)
    private int retryCount = 0;

    @Schema(description = "발행 완료 시각")
    private LocalDateTime sentAt;

    public static OutboxEvent pending(String topic, String messageKey, String payload) {
        OutboxEvent event = new OutboxEvent();
        event.topic = topic;
        event.messageKey = messageKey;
        event.payload = payload;
        event.status = OutboxEventStatus.PENDING;
        return event;
    }

    public void incrementRetryCount() {
        this.retryCount++;
    }
}
