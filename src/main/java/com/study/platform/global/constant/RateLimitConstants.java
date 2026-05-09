package com.study.platform.global.constant;

import lombok.experimental.UtilityClass;

@UtilityClass
public class RateLimitConstants {

    public static final String RATE_LIMIT_PREFIX = "rate:limit:";
    public static final int DEFAULT_LIMIT = 10;
    public static final int DEFAULT_WINDOW_SECONDS = 60;
}
