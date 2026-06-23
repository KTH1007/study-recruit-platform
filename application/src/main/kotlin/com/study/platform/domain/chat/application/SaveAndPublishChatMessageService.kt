package com.study.platform.domain.chat.application

import com.study.platform.domain.chat.dto.request.ChatMessageRequest
import com.study.platform.domain.chat.model.ChatMessage
import com.study.platform.domain.chat.model.ChatMessageRepository
import com.study.platform.domain.chat.model.ChatPublisher
import com.study.platform.domain.chat.usecase.SaveAndPublishChatMessageUseCase
import com.study.platform.domain.team.model.StudyTeamRepository
import com.study.platform.domain.team.model.TeamMemberRepository
import com.study.platform.domain.user.model.UserRepository
import com.study.platform.global.exception.CustomException
import com.study.platform.global.exception.ErrorCode
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.support.TransactionSynchronization
import org.springframework.transaction.support.TransactionSynchronizationManager
import java.util.UUID

@Service
class SaveAndPublishChatMessageService(
    private val chatMessageRepository: ChatMessageRepository,
    private val studyTeamRepository: StudyTeamRepository,
    private val teamMemberRepository: TeamMemberRepository,
    private val userRepository: UserRepository,
    private val chatPublisher: ChatPublisher
) : SaveAndPublishChatMessageUseCase {

    @Transactional
    override fun execute(userId: UUID, teamId: UUID, request: ChatMessageRequest) {
        if (!teamMemberRepository.existsByTeamIdAndUserId(teamId, userId)) {
            throw CustomException(ErrorCode.NOT_TEAM_MEMBER)
        }
        val team = studyTeamRepository.findById(teamId)
            ?: throw CustomException(ErrorCode.TEAM_NOT_FOUND)
        val sender = userRepository.findById(userId)
            ?: throw CustomException(ErrorCode.USER_NOT_FOUND)
        val message = chatMessageRepository.save(ChatMessage.create(team, sender, request.content))
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(object : TransactionSynchronization {
                override fun afterCommit() {
                    chatPublisher.publish(message)
                }
            })
        } else {
            chatPublisher.publish(message)
        }
    }
}
