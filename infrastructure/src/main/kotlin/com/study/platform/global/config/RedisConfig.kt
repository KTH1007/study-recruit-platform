package com.study.platform.global.config

import com.study.platform.domain.chat.infrastructure.RedisChatSubscriber
import com.study.platform.domain.notification.application.RedisNotificationSubscriber
import com.study.platform.global.constant.WebSocketConstants
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.listener.PatternTopic
import org.springframework.data.redis.listener.RedisMessageListenerContainer
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer
import org.springframework.data.redis.serializer.StringRedisSerializer
import tools.jackson.databind.ObjectMapper

@Configuration
class RedisConfig(
    private val objectMapper: ObjectMapper
) {

    companion object {
        private const val NOTIFICATION_CHANNEL = "notification"
        private const val ON_MESSAGE_METHOD = "onMessage"
    }

    @Bean
    fun stringRedisTemplate(connectionFactory: RedisConnectionFactory): StringRedisTemplate =
        StringRedisTemplate(connectionFactory)

    @Bean
    fun redisTemplate(connectionFactory: RedisConnectionFactory): RedisTemplate<String, Any> {
        val template = RedisTemplate<String, Any>()
        template.setConnectionFactory(connectionFactory)

        val keySerializer = StringRedisSerializer()
        val valueSerializer = JacksonJsonRedisSerializer(objectMapper, Any::class.java)

        template.keySerializer = keySerializer
        template.valueSerializer = valueSerializer
        template.hashKeySerializer = keySerializer
        template.hashValueSerializer = valueSerializer
        template.afterPropertiesSet()
        return template
    }

    @Bean
    fun redisMessageListenerContainer(
        connectionFactory: RedisConnectionFactory,
        listenerAdapter: MessageListenerAdapter,
        redisChatSubscriber: RedisChatSubscriber
    ): RedisMessageListenerContainer {
        val container = RedisMessageListenerContainer()
        container.setConnectionFactory(connectionFactory)
        container.addMessageListener(listenerAdapter, PatternTopic(NOTIFICATION_CHANNEL))
        container.addMessageListener(
            redisChatSubscriber,
            PatternTopic(WebSocketConstants.REDIS_CHAT_CHANNEL_PREFIX + "*")
        )
        return container
    }

    @Bean
    fun listenerAdapter(subscriber: RedisNotificationSubscriber): MessageListenerAdapter =
        MessageListenerAdapter(subscriber, ON_MESSAGE_METHOD)
}
