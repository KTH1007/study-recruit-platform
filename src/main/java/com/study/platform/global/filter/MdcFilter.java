package com.study.platform.global.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

public class MdcFilter extends OncePerRequestFilter {

    private static final String REQUEST_ID = "requestId";
    private static final String SERVER_INSTANCE = "serverInstance";
    private static final String CLIENT_IP = "clientIp";
    private static final String X_REQUEST_ID = "X-Request-Id";

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
        MDC.put(REQUEST_ID, UUID.randomUUID().toString());
        MDC.put(SERVER_INSTANCE, HOSTNAME);
        MDC.put(CLIENT_IP, extractClientIp(request));
        response.setHeader(X_REQUEST_ID, MDC.get(REQUEST_ID));
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
