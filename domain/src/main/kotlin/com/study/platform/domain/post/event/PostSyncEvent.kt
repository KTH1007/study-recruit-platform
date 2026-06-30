package com.study.platform.domain.post.event

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import java.util.UUID

data class PostSyncEvent @JsonCreator constructor(
    @param:JsonProperty("postId") val postId: UUID,
    @param:JsonProperty("operationType") val operationType: PostSyncOperationType,
    @param:JsonProperty("outboxEventId") val outboxEventId: Long,
    @param:JsonProperty("retryCount") val retryCount: Int
) {
    fun withRetry(): PostSyncEvent = copy(retryCount = retryCount + 1)
}
