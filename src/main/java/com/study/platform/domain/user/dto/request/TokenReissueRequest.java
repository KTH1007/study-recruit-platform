package com.study.platform.domain.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record TokenReissueRequest(

        @Schema(description = "리프레시 토큰", example = "ehd...")
        @NotBlank(message = "리프레시 토큰은 필수입니다.")
        String refreshToken
) {}
