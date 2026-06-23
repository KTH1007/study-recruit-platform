package com.study.platform.domain.team.model

import com.study.platform.domain.user.model.User
import com.study.platform.global.entity.BaseTimeEntity
import com.study.platform.global.exception.CustomException
import com.study.platform.global.exception.ErrorCode
import jakarta.persistence.*
import java.util.UUID

@Entity
@Table(name = "team_members", uniqueConstraints = [UniqueConstraint(columnNames = ["team_id", "user_id"])])
class TeamMember : BaseTimeEntity() {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "BINARY(16)")
    var id: UUID? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    var team: StudyTeam? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    var user: User? = null

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var role: TeamMemberRole = TeamMemberRole.MEMBER

    fun upgradeToLeader() { role = TeamMemberRole.LEADER }
    fun downgradeToMember() { role = TeamMemberRole.MEMBER }
    fun isLeader(): Boolean = role == TeamMemberRole.LEADER

    fun validateIsLeader() {
        if (!isLeader()) throw CustomException(ErrorCode.FORBIDDEN)
    }

    companion object {
        fun createLeader(team: StudyTeam, user: User): TeamMember = TeamMember().also {
            it.team = team
            it.user = user
            it.role = TeamMemberRole.LEADER
        }

        fun createMember(team: StudyTeam, user: User): TeamMember = TeamMember().also {
            it.team = team
            it.user = user
            it.role = TeamMemberRole.MEMBER
        }
    }
}
