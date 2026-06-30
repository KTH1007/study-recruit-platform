package com.study.platform.domain.notification.model

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import java.util.UUID

data class NotificationEvent @JsonCreator constructor(
    @param:JsonProperty("receiverId") val receiverId: UUID,
    @param:JsonProperty("type") val type: NotificationType,
    @param:JsonProperty("message") val message: String,
    @param:JsonProperty("targetId") val targetId: UUID?,
    @param:JsonProperty("retryCount") val retryCount: Int
) {
    fun withRetry(): NotificationEvent = copy(retryCount = retryCount + 1)
}
