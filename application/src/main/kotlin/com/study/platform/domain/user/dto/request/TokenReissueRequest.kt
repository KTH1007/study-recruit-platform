package com.study.platform.domain.user.dto.request

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank

data class TokenReissueRequest @JsonCreator constructor(

    @JsonProperty("refreshToken")
    @field:Schema(description = "리프레시 토큰", example = "ehd...")
    @field:NotBlank(message = "리프레시 토큰은 필수입니다.")
    val refreshToken: String
)
