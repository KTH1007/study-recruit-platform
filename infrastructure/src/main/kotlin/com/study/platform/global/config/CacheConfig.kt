package com.study.platform.global.config

import com.study.platform.domain.post.dto.response.StudyPostResponse
import com.study.platform.global.cache.RedisCacheErrorHandler
import com.study.platform.global.constant.CacheConstants
import org.springframework.cache.annotation.CachingConfigurer
import org.springframework.cache.annotation.EnableCaching
import org.springframework.cache.interceptor.CacheErrorHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.cache.RedisCacheConfiguration
import org.springframework.data.redis.cache.RedisCacheManager
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializationContext
import org.springframework.data.redis.serializer.StringRedisSerializer
import tools.jackson.databind.ObjectMapper
import java.time.Duration

@EnableCaching
@Configuration
class CacheConfig(
    private val objectMapper: ObjectMapper
) : CachingConfigurer {

    @Bean
    fun cacheManager(connectionFactory: RedisConnectionFactory): RedisCacheManager {
        val serializer = JacksonJsonRedisSerializer(objectMapper, StudyPostResponse::class.java)

        val postConfig = RedisCacheConfiguration.defaultCacheConfig()
            .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(StringRedisSerializer()))
            .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(serializer))
            .disableCachingNullValues()
            .entryTtl(Duration.ofMinutes(10))

        // cacheDefaults를 postConfig로 두면, 이후 다른 타입의 캐시가 이름 등록 없이 추가될 때
        // StudyPostResponse 전용 직렬화가 조용히 적용되어 역직렬화 오류로 이어질 수 있다.
        // 이름이 명시적으로 등록된 캐시만 타입 직렬화를 적용하고, 나머지는 프레임워크 기본값을 쓰게 한다.
        return RedisCacheManager.builder(connectionFactory)
            .withCacheConfiguration(CacheConstants.POST_CACHE, postConfig)
            .build()
    }

    override fun errorHandler(): CacheErrorHandler = RedisCacheErrorHandler()
}
