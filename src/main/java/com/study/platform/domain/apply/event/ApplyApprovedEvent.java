package com.study.platform.domain.apply.event;

import java.util.UUID;

public record ApplyApprovedEvent(
        UUID postId,
        UUID applicantId,
        String postTitle
) {
}
