package com.study.platform.domain.post.event;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "ES 동기화 이벤트")
public record PostSyncEvent(
        @Schema(description = "게시글 ID")
        UUID postId,

        @Schema(description = "ES 동기화 작업 타입")
        PostSyncOperationType operationType,

        @Schema(description = "Outbox 이벤트 ID")
        Long outboxEventId,

        int retryCount
) {
    public PostSyncEvent withRetry() {
        return new PostSyncEvent(postId, operationType, outboxEventId, retryCount + 1);
    }
}