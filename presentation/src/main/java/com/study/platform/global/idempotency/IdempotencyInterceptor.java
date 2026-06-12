package com.study.platform.global.idempotency;

import tools.jackson.databind.ObjectMapper;
import com.study.platform.global.constant.IdempotencyConstants;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class IdempotencyInterceptor implements HandlerInterceptor {

    private static final Duration PROCESSING_TTL = Duration.ofMinutes(2);
    private static final Duration CACHE_TTL = Duration.ofMinutes(30);

    private final IdempotencyObjectStoragePort idempotencyObjectStoragePort;
    private final ObjectMapper objectMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }
        if (!handlerMethod.hasMethodAnnotation(Idempotent.class)) {
            return true;
        }

        String idempotencyKey = request.getHeader(IdempotencyConstants.IDEMPOTENCY_KEY_HEADER);
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            return true;
        }

        String redisKey = buildRedisKey(idempotencyKey);

        Object cached = idempotencyObjectStoragePort.get(redisKey);
        if (cached instanceof IdempotentResponse cachedResponse) {
            log.debug("Idempotent response returned for key: {}", idempotencyKey);
            response.setStatus(cachedResponse.status());
            response.setContentType(cachedResponse.contentType());
            response.getWriter().write(cachedResponse.body());
            return false;
        }

        if (IdempotencyConstants.PROCESSING.equals(cached)) {
            writeConflictResponse(response);
            return false;
        }

        boolean acquired = idempotencyObjectStoragePort.setIfAbsent(redisKey, IdempotencyConstants.PROCESSING, PROCESSING_TTL);
        if (!acquired) {
            writeConflictResponse(response);
            return false;
        }

        request.setAttribute(IdempotencyConstants.IDEMPOTENCY_KEY_HEADER, redisKey);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) throws Exception {
        String redisKey = (String) request.getAttribute(IdempotencyConstants.IDEMPOTENCY_KEY_HEADER);
        if (redisKey == null) {
            return;
        }

        if (ex != null || response.getStatus() >= 500) {
            idempotencyObjectStoragePort.delete(redisKey);
            return;
        }

        if (response instanceof IdempotencyResponseWrapper wrapper) {
            IdempotentResponse idempotentResponse = new IdempotentResponse(
                    response.getStatus(),
                    wrapper.getCapturedBody(),
                    response.getContentType()
            );
            idempotencyObjectStoragePort.set(redisKey, idempotentResponse, CACHE_TTL);
        }
    }

    private String buildRedisKey(String idempotencyKey) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UUID userId) {
            return IdempotencyConstants.IDEMPOTENCY_PREFIX + userId + ":" + idempotencyKey;
        }
        return IdempotencyConstants.IDEMPOTENCY_PREFIX + idempotencyKey;
    }

    private void writeConflictResponse(HttpServletResponse response) throws Exception {
        response.setStatus(HttpServletResponse.SC_CONFLICT);
        response.setContentType("application/json;charset=UTF-8");
        String body = objectMapper.writeValueAsString(
                Map.of("success", false, "message", "동일한 요청이 처리 중입니다.")
        );
        response.getWriter().write(body);
    }
}
