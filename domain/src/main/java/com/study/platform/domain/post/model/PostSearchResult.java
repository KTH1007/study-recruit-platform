package com.study.platform.domain.post.model;

import java.time.LocalDateTime;
import java.util.UUID;

public record PostSearchResult(
        UUID id,
        String authorNickname,
        String title,
        String techStack,
        int maxMembers,
        LocalDateTime deadline,
        StudyPostStatus status,
        LocalDateTime createdAt
) {
}
