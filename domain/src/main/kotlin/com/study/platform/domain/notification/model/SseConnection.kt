package com.study.platform.domain.notification.model

interface SseConnection {
    @Throws(java.io.IOException::class)
    fun send(eventName: String, data: Any)
}
