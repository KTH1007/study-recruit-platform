package com.study.platform.domain.team.dto.response

import com.study.platform.domain.team.model.TeamMember
import com.study.platform.domain.team.model.TeamMemberRole
import io.swagger.v3.oas.annotations.media.Schema
import java.util.UUID

@Schema(description = "팀원 응답")
data class TeamMemberResponse(

    @field:Schema(description = "팀원 ID")
    val id: UUID,

    @field:Schema(description = "사용자 ID")
    val userId: UUID,

    @field:Schema(description = "닉네임")
    val nickname: String,

    @field:Schema(description = "역할")
    val role: TeamMemberRole
) {
    companion object {
        fun from(member: TeamMember): TeamMemberResponse = TeamMemberResponse(
            id = member.id!!,
            userId = member.user!!.id!!,
            nickname = member.user!!.nickname,
            role = member.role
        )
    }
}
