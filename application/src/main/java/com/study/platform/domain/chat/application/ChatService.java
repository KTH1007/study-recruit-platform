package com.study.platform.domain.chat.application;

import com.study.platform.domain.chat.dto.request.ChatMessageRequest;
import com.study.platform.domain.chat.dto.response.ChatMessageResponse;
import com.study.platform.domain.chat.model.ChatMessage;
import com.study.platform.domain.chat.model.ChatMessageRepository;
import com.study.platform.domain.chat.model.ChatPublisher;
import com.study.platform.domain.chat.port.ChatQueryPort;
import com.study.platform.domain.team.model.StudyTeam;
import com.study.platform.domain.team.model.StudyTeamRepository;
import com.study.platform.domain.team.model.TeamMemberRepository;
import com.study.platform.domain.user.model.User;
import com.study.platform.domain.user.model.UserRepository;
import com.study.platform.global.exception.CustomException;
import com.study.platform.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final ChatQueryPort chatQueryPort;
    private final StudyTeamRepository studyTeamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final UserRepository userRepository;
    private final ChatPublisher chatPublisher;

    @Transactional
    public void saveAndPublish(UUID userId, UUID teamId, ChatMessageRequest request) {
        validateTeamMember(teamId, userId);

        StudyTeam team = studyTeamRepository.findById(teamId)
                .orElseThrow(() -> new CustomException(ErrorCode.TEAM_NOT_FOUND));
        User sender = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        ChatMessage message = chatMessageRepository.save(ChatMessage.create(team, sender, request.content()));
        chatPublisher.publish(message);
    }

    public Slice<ChatMessageResponse> findMessages(UUID userId, UUID teamId, Pageable pageable) {
        validateTeamMember(teamId, userId);
        return chatQueryPort.findMessagesByTeamId(teamId, pageable);
    }

    private void validateTeamMember(UUID teamId, UUID userId) {
        if (!teamMemberRepository.existsByTeamIdAndUserId(teamId, userId)) {
            throw new CustomException(ErrorCode.NOT_TEAM_MEMBER);
        }
    }
}
