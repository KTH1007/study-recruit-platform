package com.study.platform.domain.chat.application

import com.study.platform.domain.chat.dto.response.ChatMessageResponse
import com.study.platform.domain.chat.port.ChatQueryPort
import com.study.platform.domain.chat.usecase.FindChatMessagesUseCase
import com.study.platform.domain.team.model.TeamMemberRepository
import com.study.platform.global.exception.CustomException
import com.study.platform.global.exception.ErrorCode
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
@Transactional(readOnly = true)
class FindChatMessagesService(
    private val chatQueryPort: ChatQueryPort,
    private val teamMemberRepository: TeamMemberRepository
) : FindChatMessagesUseCase {

    override fun execute(userId: UUID, teamId: UUID, pageable: Pageable): Slice<ChatMessageResponse> {
        if (!teamMemberRepository.existsByTeamIdAndUserId(teamId, userId)) {
            throw CustomException(ErrorCode.NOT_TEAM_MEMBER)
        }
        return chatQueryPort.findMessagesByTeamId(teamId, pageable)
    }
}
