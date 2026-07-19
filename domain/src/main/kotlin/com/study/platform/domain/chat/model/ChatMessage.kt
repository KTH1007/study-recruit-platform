package com.study.platform.domain.chat.model

import com.study.platform.domain.team.model.StudyTeam
import com.study.platform.domain.user.model.User
import com.study.platform.global.entity.BaseTimeEntity
import com.study.platform.global.exception.CustomException
import com.study.platform.global.exception.ErrorCode
import jakarta.persistence.*
import java.util.UUID

@Entity
@Table(name = "chat_messages")
class ChatMessage : BaseTimeEntity() {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "BINARY(16)")
    var id: UUID? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    var team: StudyTeam? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    var sender: User? = null

    @Column(nullable = false, columnDefinition = "TEXT")
    var content: String = ""

    companion object {
        private const val MAX_CONTENT_LENGTH = 1000

        fun create(team: StudyTeam, sender: User, content: String): ChatMessage {
            validateContent(content)
            return ChatMessage().also {
                it.team = team
                it.sender = sender
                it.content = content
            }
        }

        private fun validateContent(content: String) {
            if (content.isBlank()) throw CustomException(ErrorCode.INVALID_INPUT)
            if (content.length > MAX_CONTENT_LENGTH) throw CustomException(ErrorCode.INVALID_INPUT)
        }
    }
}
