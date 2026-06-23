package com.study.platform.global.filter

import com.study.platform.global.constant.MdcConstants
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.MDC
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.util.UUID

@Component
class MdcFilter : OncePerRequestFilter() {

    companion object {
        private val HOSTNAME: String = System.getenv("HOSTNAME") ?: "local"

        private val IP_HEADERS = arrayOf(
            "X-Forwarded-For",
            "X-Real-IP",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP"
        )
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        var requestId = request.getHeader(MdcConstants.X_REQUEST_ID)
        if (requestId.isNullOrBlank()) {
            requestId = UUID.randomUUID().toString()
        }
        MDC.put(MdcConstants.REQUEST_ID, requestId)
        MDC.put(MdcConstants.SERVER_INSTANCE, HOSTNAME)
        MDC.put(MdcConstants.CLIENT_IP, extractClientIp(request))
        MDC.put(MdcConstants.METHOD, request.method)
        MDC.put(MdcConstants.URI, request.requestURI)
        response.setHeader(MdcConstants.X_REQUEST_ID, requestId)
        try {
            filterChain.doFilter(request, response)
        } finally {
            MDC.clear()
        }
    }

    private fun extractClientIp(request: HttpServletRequest): String {
        for (header in IP_HEADERS) {
            val ip = request.getHeader(header)
            if (!ip.isNullOrBlank() && !ip.equals("unknown", ignoreCase = true)) {
                return ip.split(",")[0].trim()
            }
        }
        return request.remoteAddr
    }
}
