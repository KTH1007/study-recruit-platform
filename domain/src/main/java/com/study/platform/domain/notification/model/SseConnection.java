package com.study.platform.domain.notification.model;

import java.io.IOException;

public interface SseConnection {

    void send(String eventName, Object data) throws IOException;
}
