package com.study.platform.domain.notification.application

import com.study.platform.domain.notification.dto.response.NotificationResponse
import com.study.platform.domain.notification.model.SseEmitterPort
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import tools.jackson.core.JacksonException
import tools.jackson.databind.ObjectMapper
import java.io.IOException

@Component
class RedisNotificationSubscriber(
    private val sseEmitterPort: SseEmitterPort,
    private val objectMapper: ObjectMapper
) {
    private val log = LoggerFactory.getLogger(RedisNotificationSubscriber::class.java)!!

    companion object {
        private const val SSE_EVENT_NAME = "notification"
    }

    fun onMessage(message: String, channel: String) {
        try {
            val response = objectMapper.readValue(message, NotificationResponse::class.java)
            val connection = sseEmitterPort.findByUserId(response.receiverId) ?: return
            connection.send(SSE_EVENT_NAME, response)
        } catch (e: IOException) {
            log.warn("SSE 전송 실패 : {}", e.message)
        } catch (e: JacksonException) {
            log.warn("SSE 전송 실패 : {}", e.message)
        }
    }
}
