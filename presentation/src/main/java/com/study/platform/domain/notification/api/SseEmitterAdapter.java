package com.study.platform.domain.notification.api;

import com.study.platform.domain.notification.model.SseConnection;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

public class SseEmitterAdapter implements SseConnection {

    private final SseEmitter emitter;

    public SseEmitterAdapter(SseEmitter emitter) {
        this.emitter = emitter;
    }

    @Override
    public void send(String eventName, Object data) throws IOException {
        emitter.send(SseEmitter.event().name(eventName).data(data));
    }
}
