package com.study.platform.domain.comment.event;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "댓글 작성 이벤트 (방장 알림용)")
public record CommentCreatedEvent(

        @Schema(description = "게시글 ID")
        UUID postId,

        @Schema(description = "방장 ID")
        UUID authorId,

        @Schema(description = "댓글 작성자 ID")
        UUID commenterId,

        @Schema(description = "게시글 제목")
        String postTitle
) {}
