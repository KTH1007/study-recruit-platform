package com.study.platform.domain.post.event;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@Schema(description = "ES 동기화 작업 타입")
public enum PostSyncOperationType {

    @Schema(description = "ES 인덱스 저장 또는 갱신")
    UPSERT("ES 인덱스 저장 또는 갱신"),

    @Schema(description = "ES 인덱스 삭제")
    DELETE("ES 인덱스 삭제");

    private final String description;
}