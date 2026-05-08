package com.study.platform.global.idempotency;

import com.study.platform.global.constant.IdempotencyConstants;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class IdempotencyInterceptor implements HandlerInterceptor {

    private static final Duration TTL = Duration.ofMinutes(30);

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public boolean preHandle(HttpServletRequest request , HttpServletResponse response, Object handler) throws  Exception {
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

        String redisKey = IdempotencyConstants.IDEMPOTENCY_PREFIX + idempotencyKey;

        // 캐싱된 응답 있으면 그대로 반환
        Object cached = redisTemplate.opsForValue().get(redisKey);
        if (cached instanceof IdempotentResponse cachedResponse) {
            log.debug("Idempotent response returned for key: {}", idempotencyKey);
            response.setStatus(cachedResponse.status());
            response.setContentType(cachedResponse.contentType());
            response.getWriter().write(cachedResponse.body());
            return false;
        }

        // 처리 중이면 409 반환
        if (IdempotencyConstants.PROCESSING.equals(cached)) {
            response.setStatus(HttpServletResponse.SC_CONFLICT);
            response.setContentType("application/json");
            response.getWriter().write("{\"message\":\"동일한 요청이 처리 중입니다.\"}");
            return false;
        }

        // 처리 중 락 설정
        Boolean acquired = redisTemplate.opsForValue().setIfAbsent(redisKey, IdempotencyConstants.PROCESSING, TTL.toSeconds(), TimeUnit.SECONDS);
        if (Boolean.FALSE.equals(acquired)) {
            response.setStatus(HttpServletResponse.SC_CONFLICT);
            response.setContentType("application/json");
            response.getWriter().write("{\"message\":\"동일한 요청이 처리 중입니다.\"}");
            return false;
        }

        request.setAttribute(IdempotencyConstants.IDEMPOTENCY_KEY_HEADER, redisKey);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) throws Exception {
        String redisKey = (String) request.getAttribute(IdempotencyConstants.IDEMPOTENCY_KEY_HEADER);

        // 멱등성 키가 없으면 무시
        if (redisKey == null) {
            return;
        }

        // 예외 발생 or 500 에러 -> 락 해제 (재시도 허용)
        if (ex != null || response.getStatus() >= 500) {
            redisTemplate.delete(redisKey);
            return;
        }

        if (response instanceof IdempotencyResponseWrapper wrapper) {
            IdempotentResponse idempotentResponse = new IdempotentResponse(
                    response.getStatus(),
                    wrapper.getCapturedBody(),
                    response.getContentType()
            );
            redisTemplate.opsForValue().set(redisKey, idempotentResponse, TTL);
        }
    }
}
