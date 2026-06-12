package com.study.platform.domain.team.dto.response;

import com.study.platform.domain.team.model.StudyTeam;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "스터디팀 응답")
public record StudyTeamResponse(

        @Schema(description = "팀 ID")
        UUID id,

        @Schema(description = "게시글 ID")
        UUID postId,

        @Schema(description = "팀 이름")
        String name
) {
    public static StudyTeamResponse from(StudyTeam team) {
        return new StudyTeamResponse(
                team.getId(),
                team.getPost().getId(),
                team.getName()
        );
    }
}
