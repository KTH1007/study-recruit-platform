package com.study.platform.domain.user.dto.request

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank

data class LoginRequest @JsonCreator constructor(

    @JsonProperty("code")
    @field:Schema(description = "카카오 인가 코드", example = "abcd1234")
    @field:NotBlank(message = "인가 코드는 필수입니다.")
    val code: String
)
