package com.study.platform.global.websocket

import com.study.platform.global.constant.SecurityConstants
import com.study.platform.global.exception.CustomException
import com.study.platform.global.exception.ErrorCode
import com.study.platform.global.jwt.JwtProvider
import com.study.platform.global.ratelimit.RateLimitKeyBuilder
import com.study.platform.global.ratelimit.RateLimitStoragePort
import org.slf4j.LoggerFactory
import org.springframework.messaging.Message
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.MessagingException
import org.springframework.messaging.simp.stomp.StompCommand
import org.springframework.messaging.simp.stomp.StompHeaderAccessor
import org.springframework.messaging.support.ChannelInterceptor
import org.springframework.messaging.support.MessageHeaderAccessor
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class StompJwtInterceptor(
    private val jwtProvider: JwtProvider,
    private val rateLimitStoragePort: RateLimitStoragePort
) : ChannelInterceptor {

    private val log = LoggerFactory.getLogger(StompJwtInterceptor::class.java)

    companion object {
        private const val USER_ID_HEADER = "userId"
        private const val CHAT_RATE_LIMIT = 20L
        private const val CHAT_RATE_LIMIT_WINDOW_SECONDS = 10L
    }

    override fun preSend(message: Message<*>, channel: MessageChannel): Message<*> {
        val accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor::class.java)
            ?: return message

        when (accessor.command) {
            StompCommand.CONNECT -> handleConnect(accessor)
            StompCommand.SEND -> handleSend(accessor)
            else -> {}
        }

        return message
    }

    private fun handleConnect(accessor: StompHeaderAccessor) {
        try {
            val token = extractToken(accessor)
            val userId = jwtProvider.getUserIdFromToken(token)

            val authentication = UsernamePasswordAuthenticationToken(
                userId, null, listOf(SimpleGrantedAuthority(SecurityConstants.ROLE_USER))
            )
            accessor.user = authentication
            accessor.setNativeHeader(USER_ID_HEADER, userId.toString())
        } catch (e: CustomException) {
            log.warn("STOMP CONNECT 인증 실패 - errorCode: {}", e.errorCode, e)
            throw MessagingException(e.errorCode.message, e)
        }
    }

    private fun handleSend(accessor: StompHeaderAccessor) {
        val userId = (accessor.user as? UsernamePasswordAuthenticationToken)?.principal as? UUID
            ?: throw MessagingException(ErrorCode.INVALID_TOKEN.message)

        val key = RateLimitKeyBuilder.build("stomp", userId.toString(), accessor.destination ?: "")
        if (!rateLimitStoragePort.isAllowed(key, CHAT_RATE_LIMIT_WINDOW_SECONDS, CHAT_RATE_LIMIT)) {
            log.warn("STOMP rate limit exceeded - userId={}, destination={}", userId, accessor.destination)
            throw MessagingException(ErrorCode.TOO_MANY_REQUESTS.message)
        }
    }

    private fun extractToken(accessor: StompHeaderAccessor): String {
        val authHeader = accessor.getFirstNativeHeader(SecurityConstants.AUTHORIZATION_HEADER)
        if (authHeader == null || !authHeader.startsWith(SecurityConstants.BEARER_PREFIX)) {
            throw CustomException(ErrorCode.INVALID_TOKEN)
        }
        return authHeader.substring(SecurityConstants.BEARER_PREFIX.length)
    }
}
