package com.study.platform.global.outbox.model

enum class OutboxEventStatus(val description: String) {
    PENDING("Kafka 미발행 상태"),
    SENT("Kafka 발행 완료 상태"),
    FAILED_PERMANENTLY("최대 재시도 초과로 영구 실패")
}
