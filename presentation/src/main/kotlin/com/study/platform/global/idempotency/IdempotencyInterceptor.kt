package com.study.platform.global.idempotency

import tools.jackson.databind.ObjectMapper
import com.study.platform.global.constant.IdempotencyConstants
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.method.HandlerMethod
import org.springframework.web.servlet.HandlerInterceptor
import java.time.Duration
import java.util.UUID

@Component
class IdempotencyInterceptor(
    private val idempotencyObjectStoragePort: IdempotencyObjectStoragePort,
    private val objectMapper: ObjectMapper
) : HandlerInterceptor {

    private val log = LoggerFactory.getLogger(IdempotencyInterceptor::class.java)

    companion object {
        private val PROCESSING_TTL = Duration.ofMinutes(2)
        private val CACHE_TTL = Duration.ofMinutes(30)
    }

    @Throws(Exception::class)
    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        if (handler !is HandlerMethod) {
            return true
        }
        if (!handler.hasMethodAnnotation(Idempotent::class.java)) {
            return true
        }

        val idempotencyKey = request.getHeader(IdempotencyConstants.IDEMPOTENCY_KEY_HEADER)
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            return true
        }

        val redisKey = buildRedisKey(idempotencyKey)

        val cached = idempotencyObjectStoragePort.get(redisKey)
        if (cached is IdempotentResponse) {
            log.debug("Idempotent response returned for key: {}", idempotencyKey)
            response.status = cached.status()
            response.contentType = cached.contentType()
            response.writer.write(cached.body())
            return false
        }

        if (IdempotencyConstants.PROCESSING == cached) {
            writeConflictResponse(response)
            return false
        }

        val acquired = idempotencyObjectStoragePort.setIfAbsent(redisKey, IdempotencyConstants.PROCESSING, PROCESSING_TTL)
        if (!acquired) {
            writeConflictResponse(response)
            return false
        }

        request.setAttribute(IdempotencyConstants.IDEMPOTENCY_KEY_HEADER, redisKey)
        return true
    }

    @Throws(Exception::class)
    override fun afterCompletion(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
        ex: Exception?
    ) {
        val redisKey = request.getAttribute(IdempotencyConstants.IDEMPOTENCY_KEY_HEADER) as? String ?: return

        if (ex != null || response.status >= 500) {
            idempotencyObjectStoragePort.delete(redisKey)
            return
        }

        if (response is IdempotencyResponseWrapper) {
            val idempotentResponse = IdempotentResponse(
                response.status,
                response.capturedBody,
                response.contentType
            )
            idempotencyObjectStoragePort.set(redisKey, idempotentResponse, CACHE_TTL)
        }
    }

    private fun buildRedisKey(idempotencyKey: String): String {
        val authentication: Authentication? = SecurityContextHolder.getContext().authentication
        if (authentication != null && authentication.principal is UUID) {
            val userId = authentication.principal as UUID
            return IdempotencyConstants.IDEMPOTENCY_PREFIX + userId + ":" + idempotencyKey
        }
        return IdempotencyConstants.IDEMPOTENCY_PREFIX + idempotencyKey
    }

    @Throws(Exception::class)
    private fun writeConflictResponse(response: HttpServletResponse) {
        response.status = HttpServletResponse.SC_CONFLICT
        response.contentType = "application/json;charset=UTF-8"
        val body = objectMapper.writeValueAsString(
            mapOf("success" to false, "message" to "동일한 요청이 처리 중입니다.")
        )
        response.writer.write(body)
    }
}
