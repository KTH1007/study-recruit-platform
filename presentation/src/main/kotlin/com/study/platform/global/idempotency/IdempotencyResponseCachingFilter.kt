package com.study.platform.global.idempotency

import com.study.platform.global.constant.IdempotencyConstants
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.io.IOException

@Component
class IdempotencyResponseCachingFilter : OncePerRequestFilter() {

    @Throws(ServletException::class, IOException::class)
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val idempotencyKey = request.getHeader(IdempotencyConstants.IDEMPOTENCY_KEY_HEADER)
        if (idempotencyKey.isNullOrBlank()) {
            filterChain.doFilter(request, response)
            return
        }

        val wrapper = IdempotencyResponseWrapper(response)
        filterChain.doFilter(request, wrapper)

        response.status = wrapper.statusCode
        val body = wrapper.capturedBody
        if (body.isNotEmpty()) {
            response.contentType = wrapper.contentType ?: "application/json;charset=UTF-8"
            response.writer.write(body)
        }
    }
}
