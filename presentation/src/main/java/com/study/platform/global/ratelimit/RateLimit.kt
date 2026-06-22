package com.study.platform.global.ratelimit

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class RateLimit(
    val limit: Int = 10,
    val windowSeconds: Int = 60
)
