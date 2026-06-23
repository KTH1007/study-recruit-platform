package com.study.platform.global.config

import com.study.platform.global.constant.WebSocketConstants
import com.study.platform.global.websocket.StompJwtInterceptor
import org.springframework.context.annotation.Configuration
import org.springframework.messaging.simp.config.ChannelRegistration
import org.springframework.messaging.simp.config.MessageBrokerRegistry
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker
import org.springframework.web.socket.config.annotation.StompEndpointRegistry
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer

@Configuration
@EnableWebSocketMessageBroker
class WebSocketConfig(
    private val stompJwtInterceptor: StompJwtInterceptor
) : WebSocketMessageBrokerConfigurer {

    override fun configureMessageBroker(registry: MessageBrokerRegistry) {
        registry.enableSimpleBroker(WebSocketConstants.BROKER_PREFIX)
        registry.setApplicationDestinationPrefixes(WebSocketConstants.APP_PREFIX)
    }

    override fun registerStompEndpoints(registry: StompEndpointRegistry) {
        registry.addEndpoint(WebSocketConstants.ENDPOINT)
            .setAllowedOriginPatterns("*")
        registry.addEndpoint(WebSocketConstants.ENDPOINT)
            .setAllowedOriginPatterns("*")
            .withSockJS()
    }

    override fun configureClientInboundChannel(registration: ChannelRegistration) {
        registration.interceptors(stompJwtInterceptor)
    }
}
