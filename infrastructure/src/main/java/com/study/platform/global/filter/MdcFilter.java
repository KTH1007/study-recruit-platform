package com.study.platform.global.filter;

import com.study.platform.global.constant.MdcConstants;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
public class MdcFilter extends OncePerRequestFilter {

    private static final String HOSTNAME = System.getenv("HOSTNAME") != null
            ? System.getenv("HOSTNAME") : "local";

    private static final String[] IP_HEADERS = {
            "X-Forwarded-For",
            "X-Real-IP",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP"
    };

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String requestId = request.getHeader(MdcConstants.X_REQUEST_ID);
        if (requestId == null || requestId.isBlank()) {
            requestId = UUID.randomUUID().toString();
        }
        MDC.put(MdcConstants.REQUEST_ID, requestId);
        MDC.put(MdcConstants.SERVER_INSTANCE, HOSTNAME);
        MDC.put(MdcConstants.CLIENT_IP, extractClientIp(request));
        MDC.put(MdcConstants.METHOD, request.getMethod());
        MDC.put(MdcConstants.URI, request.getRequestURI());
        response.setHeader(MdcConstants.X_REQUEST_ID, requestId);
        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.clear();
        }
    }

    private String extractClientIp(HttpServletRequest request) {
        for (String header : IP_HEADERS) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isBlank() && !"unknown".equalsIgnoreCase(ip)) {
                return ip.split(",")[0].trim();
            }
        }
        return request.getRemoteAddr();
    }
}