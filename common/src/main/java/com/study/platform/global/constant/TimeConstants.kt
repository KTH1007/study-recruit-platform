package com.study.platform.global.constant

import java.time.ZoneId

object TimeConstants {
    const val ASIA_SEOUL = "Asia/Seoul"
    val SEOUL_ZONE: ZoneId = ZoneId.of(ASIA_SEOUL)
}
