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

        return RedisCacheManager.builder(connectionFactory)
            .cacheDefaults(postConfig)
            .withCacheConfiguration(CacheConstants.POST_CACHE, postConfig)
            .build()
    }

    override fun errorHandler(): CacheErrorHandler = RedisCacheErrorHandler()
}
