package com.study.platform.global.websocket;

import com.study.platform.global.constant.SecurityConstants;
import com.study.platform.global.exception.CustomException;
import com.study.platform.global.exception.ErrorCode;
import com.study.platform.global.jwt.JwtProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class StompJwtInterceptor implements ChannelInterceptor {

    private static final String USER_ID_HEADER = "userId";

    private final JwtProvider jwtProvider;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor == null || !StompCommand.CONNECT.equals(accessor.getCommand())) {
            return message;
        }

        String token = extractToken(accessor);
        UUID userId = jwtProvider.getUserIdFromToken(token);

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                userId, null, List.of(new SimpleGrantedAuthority(SecurityConstants.ROLE_USER))
        );
        accessor.setUser(authentication);
        accessor.setNativeHeader(USER_ID_HEADER, userId.toString());

        return message;
    }

    private String extractToken(StompHeaderAccessor accessor) {
        String authHeader = accessor.getFirstNativeHeader(SecurityConstants.AUTHORIZATION_HEADER);
        if (authHeader == null || !authHeader.startsWith(SecurityConstants.BEARER_PREFIX)) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }
        return authHeader.substring(SecurityConstants.BEARER_PREFIX.length());
    }
}
