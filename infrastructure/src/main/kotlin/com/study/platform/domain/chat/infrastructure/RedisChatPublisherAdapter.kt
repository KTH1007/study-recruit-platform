package com.study.platform.domain.chat.infrastructure

import tools.jackson.core.JacksonException
import tools.jackson.databind.ObjectMapper
import com.study.platform.domain.chat.dto.response.ChatMessageResponse
import com.study.platform.domain.chat.model.ChatMessage
import com.study.platform.domain.chat.model.ChatPublisher
import com.study.platform.global.constant.WebSocketConstants
import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component

@Component
class RedisChatPublisherAdapter(
    private val stringRedisTemplate: StringRedisTemplate,
    private val objectMapper: ObjectMapper
) : ChatPublisher {

    private val log = LoggerFactory.getLogger(RedisChatPublisherAdapter::class.java)!!

    override fun publish(message: ChatMessage) {
        try {
            val payload = objectMapper.writeValueAsString(ChatMessageResponse.from(message))
            stringRedisTemplate.convertAndSend(
                WebSocketConstants.REDIS_CHAT_CHANNEL_PREFIX + message.team!!.id!!, payload
            )
        } catch (e: JacksonException) {
            log.error("채팅 메시지 직렬화 실패", e)
        }
    }
}
