package com.study.platform.domain.chat.infrastructure;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import com.study.platform.domain.chat.dto.response.ChatMessageResponse;
import com.study.platform.domain.chat.model.ChatMessage;
import com.study.platform.domain.chat.model.ChatPublisher;
import com.study.platform.global.constant.WebSocketConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisChatPublisherAdapter implements ChatPublisher {

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public void publish(ChatMessage message) {
        try {
            String payload = objectMapper.writeValueAsString(ChatMessageResponse.from(message));
            stringRedisTemplate.convertAndSend(
                    WebSocketConstants.REDIS_CHAT_CHANNEL_PREFIX + message.getTeam().getId(), payload);
        } catch (JacksonException e) {
            log.error("채팅 메시지 직렬화 실패", e);
        }
    }
}
