package com.study.platform.global.constant;

import java.time.ZoneId;

public final class TimeConstants {

    private TimeConstants() {}

    public static final String ASIA_SEOUL = "Asia/Seoul";
    public static final ZoneId SEOUL_ZONE = ZoneId.of(ASIA_SEOUL);
}
