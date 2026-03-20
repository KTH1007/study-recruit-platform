package com.study.platform.domain.post.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@Schema(description = "스터디 모집 상태")
public enum StudyPostStatus {

    @Schema(description = "모집 중")
    OPEN("모집 중"),

    @Schema(description = "모집 마감")
    CLOSED("모집 마감"),

    @Schema(description = "정원 초과")
    FULL("정원 초과");

    private final String description;
}
