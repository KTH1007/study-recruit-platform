package com.study.platform.domain.chat.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.study.platform.domain.chat.dto.request.ChatMessageRequest;
import com.study.platform.domain.chat.dto.response.ChatMessageResponse;
import com.study.platform.domain.chat.model.ChatMessage;
import com.study.platform.domain.chat.model.ChatMessageRepository;
import com.study.platform.domain.team.model.StudyTeam;
import com.study.platform.domain.team.model.StudyTeamRepository;
import com.study.platform.domain.team.model.TeamMemberRepository;
import com.study.platform.domain.user.model.User;
import com.study.platform.domain.user.model.UserRepository;
import com.study.platform.global.constant.WebSocketConstants;
import com.study.platform.global.exception.CustomException;
import com.study.platform.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final StudyTeamRepository studyTeamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final UserRepository userRepository;
    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    @Transactional
    public void saveAndPublish(UUID userId, UUID teamId, ChatMessageRequest request) {
        validateTeamMember(teamId, userId);

        StudyTeam team = studyTeamRepository.getReferenceById(teamId);
        User sender = userRepository.getReferenceById(userId);

        ChatMessage message = chatMessageRepository.save(ChatMessage.create(team, sender, request.content()));
        publish(teamId, ChatMessageResponse.from(message));
    }

    public Slice<ChatMessageResponse> findMessages(UUID userId, UUID teamId, Pageable pageable) {
        validateTeamMember(teamId, userId);
        return chatMessageRepository.findByTeamIdWithSender(teamId, pageable)
                .map(ChatMessageResponse::from);
    }

    private void publish(UUID teamId, ChatMessageResponse response) {
        try {
            String message = objectMapper.writeValueAsString(response);
            stringRedisTemplate.convertAndSend(WebSocketConstants.REDIS_CHAT_CHANNEL_PREFIX + teamId, message);
        } catch (JsonProcessingException e) {
            log.error("채팅 메시지 직렬화 실패", e);
        }
    }

    private void validateTeamMember(UUID teamId, UUID userId) {
        if (!teamMemberRepository.existsByTeamIdAndUserId(teamId, userId)) {
            throw new CustomException(ErrorCode.NOT_TEAM_MEMBER);
        }
    }
}
