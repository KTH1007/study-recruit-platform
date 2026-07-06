package com.study.platform.global.websocket

import com.study.platform.global.constant.SecurityConstants
import com.study.platform.global.exception.CustomException
import com.study.platform.global.exception.ErrorCode
import com.study.platform.global.jwt.JwtProvider
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

@Component
class StompJwtInterceptor(
    private val jwtProvider: JwtProvider
) : ChannelInterceptor {

    private val log = LoggerFactory.getLogger(StompJwtInterceptor::class.java)

    companion object {
        private const val USER_ID_HEADER = "userId"
    }

    override fun preSend(message: Message<*>, channel: MessageChannel): Message<*> {
        val accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor::class.java)

        if (accessor == null || !StompCommand.CONNECT.equals(accessor.command)) {
            return message
        }

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

        return message
    }

    private fun extractToken(accessor: StompHeaderAccessor): String {
        val authHeader = accessor.getFirstNativeHeader(SecurityConstants.AUTHORIZATION_HEADER)
        if (authHeader == null || !authHeader.startsWith(SecurityConstants.BEARER_PREFIX)) {
            throw CustomException(ErrorCode.INVALID_TOKEN)
        }
        return authHeader.substring(SecurityConstants.BEARER_PREFIX.length)
    }
}
