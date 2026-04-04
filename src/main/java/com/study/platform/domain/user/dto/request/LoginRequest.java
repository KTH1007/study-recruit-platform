package com.study.platform.domain.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(

        @Schema(description = "카카오 인가 코드", example = "abcd1234")
        @NotBlank(message = "인가 코드는 필수입니다.")
        String code
) {}
