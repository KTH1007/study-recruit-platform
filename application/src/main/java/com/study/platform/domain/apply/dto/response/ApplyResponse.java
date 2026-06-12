package com.study.platform.domain.apply.dto.response;

import com.study.platform.domain.apply.model.Apply;
import com.study.platform.domain.apply.model.ApplyStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "지원 응답")
public record ApplyResponse(

        @Schema(description = "지원 ID")
        UUID id,

        @Schema(description = "지원자 닉네임")
        String applicantNickname,

        @Schema(description = "지원자 기술스택")
        String applicantTechStack,

        @Schema(description = "지원 메시지")
        String message,

        @Schema(description = "지원 상태")
        ApplyStatus status,

        @Schema(description = "지원 일시")
        LocalDateTime createdAt
) {
    public static ApplyResponse from(Apply application) {
        return new ApplyResponse(
                application.getId(),
                application.getApplicant().getNickname(),
                application.getApplicant().getTechStack(),
                application.getMessage(),
                application.getStatus(),
                application.getCreatedAt()
        );
    }
}
