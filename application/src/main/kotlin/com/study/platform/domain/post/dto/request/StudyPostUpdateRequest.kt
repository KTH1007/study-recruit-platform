package com.study.platform.domain.post.dto.request

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Future
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.time.LocalDateTime

data class StudyPostUpdateRequest @JsonCreator constructor(

    @JsonProperty("title")
    @field:Schema(description = "제목", example = "Spring Boot 스터디 모집합니다 (수정)")
    @field:NotBlank(message = "제목은 필수입니다.")
    val title: String,

    @JsonProperty("description")
    @field:Schema(description = "내용", example = "주 3회로 변경합니다")
    @field:NotBlank(message = "내용은 필수입니다.")
    val description: String,

    @JsonProperty("techStack")
    @field:Schema(description = "기술스택 (콤마 구분)", example = "Java,Spring,JPA")
    val techStack: String?,

    @JsonProperty("maxMembers")
    @field:Schema(description = "최대 인원", example = "6")
    @field:Min(value = 2, message = "최대 인원은 2명 이상이어야 합니다.")
    @field:Max(value = 100, message = "최대 인원은 100명 이하여야 합니다.")
    val maxMembers: Int,

    @JsonProperty("deadline")
    @field:Schema(description = "모집 마감일", example = "2026-04-15T23:59:59")
    @field:NotNull(message = "마감일은 필수입니다.")
    @field:Future(message = "마감일은 현재 시간 이후여야 합니다.")
    val deadline: LocalDateTime?
)
