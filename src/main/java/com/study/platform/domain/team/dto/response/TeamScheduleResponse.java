package com.study.platform.domain.team.dto.response;

import com.study.platform.domain.team.model.TeamSchedule;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "팀 일정 응답")
public record TeamScheduleResponse(

        @Schema(description = "일정 ID")
        UUID id,

        @Schema(description = "팀 ID")
        UUID teamId,

        @Schema(description = "일정 제목")
        String title,

        @Schema(description = "일정 내용")
        String description,

        @Schema(description = "일정 날짜")
        LocalDateTime scheduledAt,

        @Schema(description = "생성일")
        LocalDateTime createdAt
) {
    public static TeamScheduleResponse from(TeamSchedule schedule) {
        return new TeamScheduleResponse(
                schedule.getId(),
                schedule.getTeam().getId(),
                schedule.getTitle(),
                schedule.getDescription(),
                schedule.getScheduledAt(),
                schedule.getCreatedAt()
        );
    }
}