package com.study.platform.domain.chat.infrastructure

import com.study.platform.domain.chat.dto.response.ChatMessageResponse
import com.study.platform.global.constant.WebSocketConstants
import org.slf4j.LoggerFactory
import org.springframework.data.redis.connection.Message
import org.springframework.data.redis.connection.MessageListener
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Component
import tools.jackson.core.JacksonException
import tools.jackson.databind.ObjectMapper

@Component
class RedisChatSubscriber(
    private val messagingTemplate: SimpMessagingTemplate,
    private val objectMapper: ObjectMapper
) : MessageListener {

    private val log = LoggerFactory.getLogger(RedisChatSubscriber::class.java)!!

    override fun onMessage(message: Message, pattern: ByteArray?) {
        try {
            val response = objectMapper.readValue(message.body, ChatMessageResponse::class.java)
            messagingTemplate.convertAndSend(
                WebSocketConstants.CHAT_TOPIC_PREFIX + response.teamId, response
            )
        } catch (e: JacksonException) {
            log.error("Redis 채팅 메시지 처리 실패", e)
        }
    }
}
