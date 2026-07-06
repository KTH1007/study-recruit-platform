package com.study.platform.global.idempotency

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import java.io.Serializable

data class IdempotentResponse @JsonCreator constructor(
    @JsonProperty("status") val status: Int,
    @JsonProperty("body") val body: String,
    @JsonProperty("contentType") val contentType: String
) : Serializable
