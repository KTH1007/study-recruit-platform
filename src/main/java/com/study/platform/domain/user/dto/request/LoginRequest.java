package com.study.platform.domain.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(

        @Schema(description = "카카오 인가 코드", example = "abcd1234")
        @NotBlank
        String code
) {}
