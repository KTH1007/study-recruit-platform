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
            if (delimiterIndex == -1) {
                // 레거시 포맷: 구분자 없는 단순 바디 → 200으로 처리
                response.status = HttpServletResponse.SC_OK
                if (cached.isNotEmpty()) {
                    response.contentType = "application/json;charset=UTF-8"
                    response.writer.write(cached)
                }
            } else {
                val status = cached.substring(0, delimiterIndex).toInt()
                val body = cached.substring(delimiterIndex + 1)
                response.status = status
                if (body.isNotEmpty()) {
                    response.contentType = "application/json;charset=UTF-8"
                    response.writer.write(body)
                }
            }
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

            if (capturedStatus >= 500) {
                // 5xx는 캐시하지 않고 키 삭제 → 클라이언트가 안전하게 재시도 가능
                idempotencyStoragePort.delete(redisKey)
            } else {
                idempotencyStoragePort.set(redisKey, "$capturedStatus|$capturedBody", TTL)
            }

            response.status = capturedStatus
            val contentType = wrapper.contentType
            if (capturedBody.isNotEmpty()) {
                response.contentType = contentType ?: "application/json;charset=UTF-8"
                response.writer.write(capturedBody)
            }
        } catch (e: Exception) {
            idempotencyStoragePort.delete(redisKey)
            throw e
        }
    }
}
