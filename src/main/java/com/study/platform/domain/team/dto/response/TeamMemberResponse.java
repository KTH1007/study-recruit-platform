package com.study.platform.domain.team.dto.response;

import com.study.platform.domain.team.model.TeamMember;
import com.study.platform.domain.team.model.TeamMemberRole;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "팀원 응답")
public record TeamMemberResponse(

        @Schema(description = "팀원 ID")
        UUID id,

        @Schema(description = "사용자 ID")
        UUID userId,

        @Schema(description = "닉네임")
        String nickname,

        @Schema(description = "역할")
        TeamMemberRole role
) {
    public static TeamMemberResponse from(TeamMember member) {
        return new TeamMemberResponse(
                member.getId(),
                member.getUser().getId(),
                member.getUser().getNickname(),
                member.getRole()
        );
    }
}
