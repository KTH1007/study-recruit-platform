package com.study.platform.domain.apply.event;

import java.util.UUID;

public record ApplyRejectedEvent(
        UUID postId,
        UUID applicantId,
        String postTitle
) {
}
