package com.study.platform.domain.team.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

@Schema(description = "팀 일정 수정 요청")
public record TeamScheduleUpdateRequest(

        @Schema(description = "일정 제목", example = "2회차 스터디 미팅")
        @NotBlank(message = "일정 제목은 필수입니다.")
        String title,

        @Schema(description = "일정 내용", example = "2회차 스터디 미팅 내용")
        String description,

        @Schema(description = "일정 날짜", example = "2026-05-08T14:00:00")
        @NotNull(message = "일정 날짜는 필수입니다.")
        LocalDateTime scheduledAt
) {}
