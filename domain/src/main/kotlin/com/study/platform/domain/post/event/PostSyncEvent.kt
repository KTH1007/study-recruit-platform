package com.study.platform.domain.post.event

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import java.util.UUID

data class PostSyncEvent @JsonCreator constructor(
    @JsonProperty("postId") val postId: UUID,
    @JsonProperty("operationType") val operationType: PostSyncOperationType,
    @JsonProperty("outboxEventId") val outboxEventId: Long,
    @JsonProperty("retryCount") val retryCount: Int
) {
    fun withRetry(): PostSyncEvent = copy(retryCount = retryCount + 1)
}
