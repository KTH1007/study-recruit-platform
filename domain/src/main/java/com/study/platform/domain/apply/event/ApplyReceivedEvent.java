package com.study.platform.domain.apply.event;

import java.util.UUID;

public record ApplyReceivedEvent(
        UUID postId,
        UUID authorId,
        String postTitle
) {}