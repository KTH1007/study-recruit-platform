package com.study.platform.global.ratelimit;

import com.study.platform.global.constant.RateLimitConstants;
import com.study.platform.global.exception.CustomException;
import com.study.platform.global.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimitInterceptor implements HandlerInterceptor {

    private final RateLimitStoragePort rateLimitStoragePort;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        RateLimit rateLimit = handlerMethod.getMethodAnnotation(RateLimit.class);
        if (rateLimit == null) {
            return true;
        }

        String userId = resolveUserId();
        if (userId == null) {
            return true;
        }

        String pattern = (String) request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
        String endpoint = request.getMethod() + ":" + pattern;
        String key = RateLimitConstants.RATE_LIMIT_PREFIX + userId + ":" + endpoint;

        if (!rateLimitStoragePort.isAllowed(key, rateLimit.windowSeconds(), rateLimit.limit())) {
            log.warn("Rate limit exceeded. userId={}, endpoint={}", userId, endpoint);
            throw new CustomException(ErrorCode.TOO_MANY_REQUESTS);
        }

        return true;
    }

    private String resolveUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UUID userId) {
            return userId.toString();
        }
        return null;
    }
}
