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
            val delimiterIndex = cached.indexOf('|')
            val status = cached.substring(0, delimiterIndex).toInt()
            val body = cached.substring(delimiterIndex + 1)
            response.status = status
            response.contentType = "application/json;charset=UTF-8"
            response.writer.write(body)
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
            val capturedBody = wrapper.capturedBody
            val capturedStatus = wrapper.statusCode
            idempotencyStoragePort.set(redisKey, "$capturedStatus|$capturedBody", TTL)
            response.status = capturedStatus
            response.contentType = "application/json;charset=UTF-8"
            response.writer.write(capturedBody)
        } catch (e: Exception) {
            idempotencyStoragePort.delete(redisKey)
            throw e
        }
    }
}
