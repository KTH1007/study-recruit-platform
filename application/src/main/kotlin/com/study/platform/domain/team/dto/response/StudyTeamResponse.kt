package com.study.platform.domain.team.dto.response

import com.study.platform.domain.team.model.StudyTeam
import io.swagger.v3.oas.annotations.media.Schema
import java.util.UUID

@Schema(description = "스터디팀 응답")
data class StudyTeamResponse(

    @field:Schema(description = "팀 ID")
    val id: UUID,

    @field:Schema(description = "게시글 ID")
    val postId: UUID,

    @field:Schema(description = "팀 이름")
    val name: String
) {
    companion object {
        fun from(team: StudyTeam): StudyTeamResponse = StudyTeamResponse(
            id = team.id!!,
            postId = team.post!!.id!!,
            name = team.name
        )
    }
}
