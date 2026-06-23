package com.study.platform.global.cache

import org.slf4j.LoggerFactory
import org.springframework.cache.Cache
import org.springframework.cache.interceptor.CacheErrorHandler

class RedisCacheErrorHandler : CacheErrorHandler {

    private val log = LoggerFactory.getLogger(RedisCacheErrorHandler::class.java)!!

    override fun handleCacheGetError(e: RuntimeException, cache: Cache, key: Any) {
        log.warn("Redis 캐시 조회 실패 - cache: {}, key: {}, error: {}", cache.name, key, e.message)
    }

    override fun handleCachePutError(e: RuntimeException, cache: Cache, key: Any, value: Any?) {
        log.warn("Redis 캐시 저장 실패 - cache: {}, key: {}, error: {}", cache.name, key, e.message)
    }

    override fun handleCacheEvictError(e: RuntimeException, cache: Cache, key: Any) {
        log.warn("Redis 캐시 삭제 실패 - cache: {}, key: {}, error: {}", cache.name, key, e.message)
    }

    override fun handleCacheClearError(e: RuntimeException, cache: Cache) {
        log.warn("Redis 캐시 전체 삭제 실패 - cache: {}, error: {}", cache.name, e.message)
    }
}
