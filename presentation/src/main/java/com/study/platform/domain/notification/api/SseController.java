package com.study.platform.domain.notification.api;

import com.study.platform.domain.notification.api.doc.SseControllerDoc;
import com.study.platform.domain.notification.model.SseEmitterPort;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
public class SseController implements SseControllerDoc {

    private static final long SSE_TIMEOUT = 30 * 60 * 1000L;

    private final SseEmitterPort sseEmitterPort;

    @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(@AuthenticationPrincipal UUID userId) {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);
        SseEmitterAdapter connection = new SseEmitterAdapter(emitter);

        sseEmitterPort.save(userId, connection);

        emitter.onCompletion(() -> sseEmitterPort.delete(userId));
        emitter.onTimeout(() -> sseEmitterPort.delete(userId));
        emitter.onError(e -> sseEmitterPort.delete(userId));

        return emitter;
    }
}
