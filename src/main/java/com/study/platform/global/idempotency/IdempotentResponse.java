package com.study.platform.global.idempotency;

import java.io.Serializable;

public record IdempotentResponse(
        int status,
        String body,
        String contentType
) implements Serializable {
}
