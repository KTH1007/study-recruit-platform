package com.study.platform.domain.post.event;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "모집 마감 D-1 리마인더 이벤트")
public record PostDeadlineReminderEvent(

        @Schema(description = "게시글 ID")
        UUID postId,
        @Schema(description = "방장 ID")
        UUID authorId,
        @Schema(description = "게시글 제목")
        String postTitle
) {}
