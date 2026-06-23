package com.study.platform.global.ratelimit

import com.study.platform.global.constant.RateLimitConstants
import com.study.platform.global.exception.CustomException
import com.study.platform.global.exception.ErrorCode
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.method.HandlerMethod
import org.springframework.web.servlet.HandlerInterceptor
import org.springframework.web.servlet.HandlerMapping
import java.util.UUID

@Component
class RateLimitInterceptor(
    private val rateLimitStoragePort: RateLimitStoragePort
) : HandlerInterceptor {

    private val log = LoggerFactory.getLogger(RateLimitInterceptor::class.java)

    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        if (handler !is HandlerMethod) {
            return true
        }

        val rateLimit = handler.getMethodAnnotation(RateLimit::class.java) ?: return true

        val userId = resolveUserId() ?: return true

        val pattern = request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE) as? String
        val endpoint = "${request.method}:$pattern"
        val key = RateLimitConstants.RATE_LIMIT_PREFIX + userId + ":" + endpoint

        if (!rateLimitStoragePort.isAllowed(key, rateLimit.windowSeconds.toLong(), rateLimit.limit.toLong())) {
            log.warn("Rate limit exceeded. userId={}, endpoint={}", userId, endpoint)
            throw CustomException(ErrorCode.TOO_MANY_REQUESTS)
        }

        return true
    }

    private fun resolveUserId(): String? {
        val authentication: Authentication? = SecurityContextHolder.getContext().authentication
        if (authentication != null && authentication.principal is UUID) {
            return (authentication.principal as UUID).toString()
        }
        return null
    }
}
