package com.study.platform.domain.chat.infrastructure;

import com.study.platform.domain.chat.dto.response.ChatMessageResponse;
import com.study.platform.global.constant.WebSocketConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisChatSubscriber implements MessageListener {

    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            ChatMessageResponse response = objectMapper.readValue(message.getBody(), ChatMessageResponse.class);
            messagingTemplate.convertAndSend(
                    WebSocketConstants.CHAT_TOPIC_PREFIX + response.teamId(), response
            );
        } catch (IOException e) {
            log.error("Redis 채팅 메시지 처리 실패", e);
        }
    }
}
