package com.study.platform.domain.notification.model

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import java.util.UUID

data class NotificationEvent @JsonCreator constructor(
    @JsonProperty("receiverId") val receiverId: UUID,
    @JsonProperty("type") val type: NotificationType,
    @JsonProperty("message") val message: String,
    @JsonProperty("targetId") val targetId: UUID?,
    @JsonProperty("retryCount") val retryCount: Int
) {
    fun withRetry(): NotificationEvent = copy(retryCount = retryCount + 1)
}
