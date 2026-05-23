package com.study.platform.global.outbox.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@Schema(description = "Outbox 이벤트 발행 상태")
public enum OutboxEventStatus {

    @Schema(description = "Kafka 미발행 상태")
    PENDING("Kafka 미발행 상태"),

    @Schema(description = "Kafka 발행 완료 상태")
    SENT("Kafka 발행 완료 상태");

    private final String description;
}
