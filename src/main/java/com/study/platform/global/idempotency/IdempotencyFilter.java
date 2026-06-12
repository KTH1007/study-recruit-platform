package com.study.platform.global.idempotency;

import com.study.platform.global.constant.IdempotencyConstants;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;

@Component
@RequiredArgsConstructor
public class IdempotencyFilter extends OncePerRequestFilter {

    private static final Duration TTL = Duration.ofHours(24);

    private final IdempotencyStoragePort idempotencyStoragePort;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String idempotencyKey = request.getHeader(IdempotencyConstants.IDEMPOTENCY_KEY_HEADER);
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            filterChain.doFilter(request, response);
            return;
        }

        String uri = request.getRequestURI();
        if (!uri.startsWith("/api/")) {
            filterChain.doFilter(request, response);
            return;
        }

        String redisKey = IdempotencyConstants.IDEMPOTENCY_PREFIX + idempotencyKey;

        String cached = idempotencyStoragePort.get(redisKey);
        if (cached != null && !cached.equals(IdempotencyConstants.PROCESSING)) {
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(cached);
            return;
        }

        boolean acquired = idempotencyStoragePort.setIfAbsent(redisKey, IdempotencyConstants.PROCESSING, TTL);
        if (!acquired) {
            response.sendError(HttpServletResponse.SC_CONFLICT, "요청 처리 중입니다. 잠시 후 다시 시도해주세요.");
            return;
        }

        IdempotencyResponseWrapper wrapper = new IdempotencyResponseWrapper(response);
        try {
            filterChain.doFilter(request, wrapper);
            wrapper.flushBuffer();
            idempotencyStoragePort.set(redisKey, wrapper.getCapturedBody(), TTL);
        } catch (Exception e) {
            idempotencyStoragePort.delete(redisKey);
            throw e;
        }
    }
}
