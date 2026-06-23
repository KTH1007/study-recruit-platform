package com.study.platform.global.idempotency

import com.study.platform.global.constant.IdempotencyConstants
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.io.IOException
import java.time.Duration

@Component
class IdempotencyFilter(
    private val idempotencyStoragePort: IdempotencyStoragePort
) : OncePerRequestFilter() {

    companion object {
        private val TTL = Duration.ofHours(24)
    }

    @Throws(ServletException::class, IOException::class)
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val idempotencyKey = request.getHeader(IdempotencyConstants.IDEMPOTENCY_KEY_HEADER)
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            filterChain.doFilter(request, response)
            return
        }

        val uri = request.requestURI
        if (!uri.startsWith("/api/")) {
            filterChain.doFilter(request, response)
            return
        }

        val redisKey = IdempotencyConstants.IDEMPOTENCY_PREFIX + idempotencyKey

        val cached = idempotencyStoragePort.get(redisKey)
        if (cached != null && cached != IdempotencyConstants.PROCESSING) {
            response.contentType = "application/json;charset=UTF-8"
            response.writer.write(cached)
            return
        }

        val acquired = idempotencyStoragePort.setIfAbsent(redisKey, IdempotencyConstants.PROCESSING, TTL)
        if (!acquired) {
            response.sendError(HttpServletResponse.SC_CONFLICT, "요청 처리 중입니다. 잠시 후 다시 시도해주세요.")
            return
        }

        val wrapper = IdempotencyResponseWrapper(response)
        try {
            filterChain.doFilter(request, wrapper)
            wrapper.flushBuffer()
            idempotencyStoragePort.set(redisKey, wrapper.capturedBody, TTL)
        } catch (e: Exception) {
            idempotencyStoragePort.delete(redisKey)
            throw e
        }
    }
}
