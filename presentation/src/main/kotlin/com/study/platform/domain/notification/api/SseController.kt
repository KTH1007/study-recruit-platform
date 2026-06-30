package com.study.platform.domain.notification.api

import com.study.platform.domain.notification.api.doc.SseControllerDoc
import com.study.platform.domain.notification.model.SseEmitterPort
import org.springframework.http.MediaType
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.util.UUID

@RestController
@RequestMapping("/api/notifications")
class SseController(
    private val sseEmitterPort: SseEmitterPort
) : SseControllerDoc {

    companion object {
        private const val SSE_TIMEOUT = 30 * 60 * 1000L
    }

    @GetMapping(value = ["/subscribe"], produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    override fun subscribe(@AuthenticationPrincipal userId: UUID): SseEmitter {
        val emitter = SseEmitter(SSE_TIMEOUT)
        val connection = SseEmitterAdapter(emitter)

        sseEmitterPort.save(userId, connection)

        emitter.onCompletion { sseEmitterPort.delete(userId) }
        emitter.onTimeout { sseEmitterPort.delete(userId) }
        emitter.onError { sseEmitterPort.delete(userId) }

        emitter.send(SseEmitter.event().name("connect").data("connected"))

        return emitter
    }
}
