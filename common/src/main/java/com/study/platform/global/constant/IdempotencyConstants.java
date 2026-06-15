package com.study.platform.global.constant;

import lombok.experimental.UtilityClass;

@UtilityClass
public class IdempotencyConstants {

    public static final String IDEMPOTENCY_KEY_HEADER = "Idempotency-Key";
    public static final String IDEMPOTENCY_PREFIX = "idempotency:";
    public static final String PROCESSING = "PROCESSING";
}