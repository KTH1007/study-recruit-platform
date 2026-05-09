package com.study.platform.global.ratelimit;

import com.study.platform.global.constant.RateLimitConstants;
import com.study.platform.global.exception.CustomException;
import com.study.platform.global.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimitInterceptor implements HandlerInterceptor{

    private final RedisTemplate<String, Object> redisTemplate;

    // Lua Script
    private static final String SLIDING_WINDOW_SCRIPT = """
              local key = KEYS[1]
              local now = tonumber(ARGV[1])
              local window = tonumber(ARGV[2])
              local limit = tonumber(ARGV[3])
              local clearBefore = now - window * 1000

              redis.call('ZREMRANGEBYSCORE', key, '-inf', clearBefore)
              local count = redis.call('ZCARD', key)

              if count < limit then
                  redis.call('ZADD', key, now, now)
                  redis.call('PEXPIRE', key, window * 1000)
                  return 1
              end
              return 0
              """;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) {
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

        String endpoint = request.getMethod() + ":" + request.getRequestURI();
        String redisKey = RateLimitConstants.RATE_LIMIT_PREFIX + userId + ":" + endpoint;

        Long result = redisTemplate.execute(
                RedisScript.of(SLIDING_WINDOW_SCRIPT, Long.class),
                List.of(redisKey),
                System.currentTimeMillis(),
                rateLimit.windowSeconds(),
                rateLimit.limit()
        );

        if (!Long.valueOf(1L).equals(result)) {
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
