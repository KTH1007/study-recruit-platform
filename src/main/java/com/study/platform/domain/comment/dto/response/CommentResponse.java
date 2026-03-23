package com.study.platform.domain.comment.dto.response;

import com.study.platform.domain.comment.model.Comment;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "댓글 응답")
public record CommentResponse(

        @Schema(description = "댓글 ID")
        UUID id,

        @Schema(description = "작성자 ID")
        UUID authorId,

        @Schema(description = "작성자 닉네임")
        String authorNickname,

        @Schema(description = "댓글 내용")
        String content,

        @Schema(description = "작성 시각")
        LocalDateTime createdAt
) {
    public static CommentResponse from(Comment comment) {
        return new CommentResponse(
                comment.getId(),
                comment.getAuthor().getId(),
                comment.getAuthor().getNickname(),
                comment.getContent(),
                comment.getCreatedAt()
        );
    }
}
