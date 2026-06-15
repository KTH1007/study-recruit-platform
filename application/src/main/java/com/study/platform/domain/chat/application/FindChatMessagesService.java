package com.study.platform.domain.chat.application;

import com.study.platform.domain.chat.dto.response.ChatMessageResponse;
import com.study.platform.domain.chat.port.ChatQueryPort;
import com.study.platform.domain.chat.usecase.FindChatMessagesUseCase;
import com.study.platform.domain.team.model.TeamMemberRepository;
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
public class FindChatMessagesService implements FindChatMessagesUseCase {

    private final ChatQueryPort chatQueryPort;
    private final TeamMemberRepository teamMemberRepository;

    @Override
    public Slice<ChatMessageResponse> execute(UUID userId, UUID teamId, Pageable pageable) {
        if (!teamMemberRepository.existsByTeamIdAndUserId(teamId, userId)) {
            throw new CustomException(ErrorCode.NOT_TEAM_MEMBER);
        }
        return chatQueryPort.findMessagesByTeamId(teamId, pageable);
    }
}
