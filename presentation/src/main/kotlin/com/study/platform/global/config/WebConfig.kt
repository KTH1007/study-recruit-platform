package com.study.platform.global.config

import com.study.platform.global.idempotency.IdempotencyInterceptor
import com.study.platform.global.ratelimit.RateLimitInterceptor
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebConfig(
    private val idempotencyInterceptor: IdempotencyInterceptor,
    private val rateLimitInterceptor: RateLimitInterceptor
) : WebMvcConfigurer {

    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(idempotencyInterceptor)
            .addPathPatterns("/api/**")
        registry.addInterceptor(rateLimitInterceptor)
            .addPathPatterns("/api/**")
    }
}
