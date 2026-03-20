package com.study.platform.domain.post.dto.request;

import com.study.platform.domain.post.model.StudyPost;
import com.study.platform.domain.user.model.User;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record StudyPostCreateRequest(

        @Schema(description = "제목", example = "Spring Boot 스터디 모집합니다")
        @NotBlank
        String title,

        @Schema(description = "내용", example = "주 2회 온라인으로 진행합니다")
        @NotBlank
        String description,

        @Schema(description = "기술스택 (콤마 구분)", example = "Java,Spring,JPA")
        String techStack,

        @Schema(description = "최대 인원", example = "5")
        @Min(2)
        int maxMembers,

        @Schema(description = "모집 마감일", example = "2026-04-01T23:59:59")
        @NotNull
        @Future
        LocalDateTime deadline
) {
    public StudyPost toEntity(User author) {
        return StudyPost.create(author, title, description, techStack, maxMembers, deadline);
    }
}
