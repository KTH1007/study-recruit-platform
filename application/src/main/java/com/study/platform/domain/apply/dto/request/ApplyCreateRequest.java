package com.study.platform.domain.apply.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(description = "지원 요청")
public record ApplyCreateRequest(

        @Size(max = 500, message = "지원 메시지는 500자 이하여야 합니다.")
        @Schema(description = "지원 메시지", example = "열심히 하겠습니다,")
        String message
) {}
