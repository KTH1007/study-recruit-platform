package com.study.platform.domain.team.model;

import com.study.platform.domain.post.model.StudyPost;
import com.study.platform.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "study_teams")
public class StudyTeam extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false, unique = true)
    private StudyPost post;

    @Column(length = 100, nullable = false)
    private String name;

    @Builder
    private StudyTeam(StudyPost post, String name) {
        this.post = post;
        this.name = name;
    }

    public static StudyTeam create(StudyPost post) {
        return StudyTeam.builder()
                .post(post)
                .name(post.getTitle())
                .build();
    }
}
