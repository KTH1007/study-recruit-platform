package com.study.platform.domain.notification.api

import com.study.platform.domain.notification.model.SseConnection
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.io.IOException

class SseEmitterAdapter(
    private val emitter: SseEmitter
) : SseConnection {

    @Throws(IOException::class)
    override fun send(eventName: String, data: Any) {
        emitter.send(SseEmitter.event().name(eventName).data(data))
    }
}
