package com.study.platform.domain.team.dto.request

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.time.LocalDateTime

@Schema(description = "팀 일정 생성 요청")
data class TeamScheduleCreateRequest @JsonCreator constructor(

    @JsonProperty("title")
    @field:Schema(description = "일정 제목", example = "1회차 스터디 미팅")
    @field:NotBlank(message = "일정 제목은 필수입니다.")
    @field:Size(max = 100, message = "일정 제목은 100자 이하로 입력해주세요.")
    val title: String,

    @JsonProperty("description")
    @field:Schema(description = "일정 내용", example = "1회차 스터디 미팅 내용")
    @field:Size(max = 1000, message = "일정 내용은 1000자 이하로 입력해주세요.")
    val description: String?,

    @JsonProperty("scheduledAt")
    @field:Schema(description = "일정 날짜", example = "2026-05-01T14:00:00")
    @field:NotNull(message = "일정 날짜는 필수입니다.")
    val scheduledAt: LocalDateTime?
)
