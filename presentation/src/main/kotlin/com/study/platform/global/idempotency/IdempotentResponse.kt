package com.study.platform.global.idempotency

import java.io.Serializable

data class IdempotentResponse(
    private val status: Int,
    private val body: String,
    private val contentType: String
) : Serializable {
    fun status(): Int = status
    fun body(): String = body
    fun contentType(): String = contentType
}
