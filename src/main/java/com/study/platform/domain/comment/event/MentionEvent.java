package com.study.platform.domain.comment.event;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "댓글 멘션 이벤트 (멘션된 유저 알림용)")
public record MentionEvent(

        @Schema(description = "게시글 ID")
        UUID postId,

        @Schema(description = "멘션된 유저 ID")
        UUID mentionedUserId,

        @Schema(description = "댓글 작성자 닉네임")
        String commenterNickname,

        @Schema(description = "게시글 제목")
        String postTitle
) {}
