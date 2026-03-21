package com.study.platform.domain.apply.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@Schema(description = "지원 상태")
public enum ApplyStatus {

    @Schema(description = "검토중")
    PENDING("검토중"),

    @Schema(description = "승인")
    APPROVED("승인"),

    @Schema(description = "거절")
    REJECTED("거절");

    private final String description;
}
