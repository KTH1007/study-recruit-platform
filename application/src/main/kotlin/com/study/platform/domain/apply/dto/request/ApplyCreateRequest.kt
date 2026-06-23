package com.study.platform.domain.apply.dto.request

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.constraints.Size

data class ApplyCreateRequest @JsonCreator constructor(
    @JsonProperty("message")
    @field:Size(max = 500, message = "지원 메시지는 500자 이하여야 합니다.")
    val message: String
)
