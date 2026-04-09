package com.study.platform.global.constant;

import lombok.experimental.UtilityClass;

import java.time.ZoneId;

@UtilityClass
public class TimeConstants {

    public static final String ASIA_SEOUL = "Asia/Seoul";
    public static final ZoneId SEOUL_ZONE = ZoneId.of(ASIA_SEOUL);
}
