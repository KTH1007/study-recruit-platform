package com.study.platform.domain.chat.model

import com.study.platform.domain.team.model.StudyTeam
import com.study.platform.domain.user.model.User
import com.study.platform.global.entity.BaseTimeEntity
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
        fun create(team: StudyTeam, sender: User, content: String): ChatMessage = ChatMessage().also {
            it.team = team
            it.sender = sender
            it.content = content
        }
    }
}
