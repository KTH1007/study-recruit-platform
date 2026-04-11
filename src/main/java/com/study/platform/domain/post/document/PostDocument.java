package com.study.platform.domain.post.document;

import com.study.platform.domain.post.model.StudyPost;
import com.study.platform.domain.post.model.StudyPostStatus;
import org.springframework.data.annotation.Id;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.elasticsearch.annotations.*;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Document(indexName = "study_posts")
@Setting(settingPath = "elasticsearch/post-settings.json")
public class PostDocument {

    @Id
    private String id;

    @Field(type = FieldType.Text, analyzer = "nori_analyzer")
    private String title;

    @Field(type = FieldType.Text, analyzer = "nori_analyzer")
    private String description;

    @MultiField(
            mainField = @Field(type = FieldType.Text, analyzer = "nori_analyzer"),
            otherFields = @InnerField(suffix = "keyword", type = FieldType.Keyword)
    )
    private String techStack;

    @Field(type = FieldType.Keyword)
    private String status;

    @Field(type = FieldType.Integer)
    private int maxMembers;

    @Field(type = FieldType.Date, format = DateFormat.date_time)
    private LocalDateTime deadline;

    @Field(type = FieldType.Keyword)
    private String authorNickname;

    @Field(type = FieldType.Date, format = DateFormat.date_time)
    private LocalDateTime createdAt;

    @Builder
    public PostDocument(String id, String title, String description, String techStack, String status, int maxMembers,
                        LocalDateTime deadline, String authorNickname, LocalDateTime createdAt) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.techStack = techStack;
        this.status = status;
        this.maxMembers = maxMembers;
        this.deadline = deadline;
        this.authorNickname = authorNickname;
        this.createdAt = createdAt;
    }

    public static PostDocument from(StudyPost post) {
        return PostDocument.builder()
                .id(post.getId().toString())
                .title(post.getTitle())
                .description(post.getDescription())
                .techStack(post.getTechStack())
                .status(post.getStatus().name())
                .maxMembers(post.getMaxMembers())
                .deadline(post.getDeadline())
                .authorNickname(post.getAuthor().getNickname())
                .createdAt(post.getCreatedAt())
                .build();
    }

    public void updateStatus(StudyPostStatus status) {
        this.status = status.name();
    }
}
