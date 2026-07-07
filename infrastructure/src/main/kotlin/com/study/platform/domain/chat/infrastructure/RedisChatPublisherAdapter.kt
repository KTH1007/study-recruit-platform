package com.study.platform.domain.chat.infrastructure

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
        // 채팅 메시지는 이미 DB에 저장된 뒤이므로, 발행 실패는 저장 자체를 실패시키지 않고 로그만 남긴다.
        try {
            val teamId = message.team?.id
            if (teamId == null) {
                log.error("채팅 메시지 발행 실패 - team 정보가 없습니다. messageId={}", message.id)
                return
            }
            val payload = objectMapper.writeValueAsString(ChatMessageResponse.from(message))
            stringRedisTemplate.convertAndSend(WebSocketConstants.REDIS_CHAT_CHANNEL_PREFIX + teamId, payload)
        } catch (e: Exception) {
            log.error("채팅 메시지 발행 실패 - messageId={}", message.id, e)
        }
    }
}
