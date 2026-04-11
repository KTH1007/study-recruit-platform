package com.study.platform.domain.post.dto.response;

import com.study.platform.domain.post.document.PostDocument;
import com.study.platform.domain.post.model.StudyPost;
import com.study.platform.domain.post.model.StudyPostStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.UUID;

public record StudyPostSummaryResponse(

        @Schema(description = "게시글 ID")
        UUID id,

        @Schema(description = "작성자 닉네임")
        String authorNickname,

        @Schema(description = "제목")
        String title,

        @Schema(description = "기술스택")
        String techStack,

        @Schema(description = "최대 인원")
        int maxMembers,

        @Schema(description = "모집 마감일")
        LocalDateTime deadline,

        @Schema(description = "모집 상태")
        StudyPostStatus status,

        @Schema(description = "작성일")
        LocalDateTime createdAt
) {
    public static StudyPostSummaryResponse from(StudyPost post) {
        return new StudyPostSummaryResponse(
                post.getId(),
                post.getAuthor().getNickname(),
                post.getTitle(),
                post.getTechStack(),
                post.getMaxMembers(),
                post.getDeadline(),
                post.getStatus(),
                post.getCreatedAt()
        );
    }

    public static StudyPostSummaryResponse fromDocument(PostDocument document) {
        return new StudyPostSummaryResponse(
                UUID.fromString(document.getId()),
                document.getAuthorNickname(),
                document.getTitle(),
                document.getTechStack(),
                document.getMaxMembers(),
                document.getDeadline(),
                StudyPostStatus.valueOf(document.getStatus()),
                document.getCreatedAt()
        );
    }
}
