package com.study.platform.domain.post.event

enum class PostSyncOperationType(val description: String) {
    UPSERT("ES 인덱스 저장 또는 갱신"),
    DELETE("ES 인덱스 삭제")
}
