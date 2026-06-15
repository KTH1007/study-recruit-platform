package com.study.platform.domain.team.model;

import com.study.platform.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "team_schedules")
public class TeamSchedule extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private StudyTeam team;

    @Column(length = 100, nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private LocalDateTime scheduledAt;

    @Builder
    private TeamSchedule(StudyTeam team, String title, String description, LocalDateTime scheduledAt) {
        this.team = team;
        this.title = title;
        this.description = description;
        this.scheduledAt = scheduledAt;
    }

    public static TeamSchedule create(StudyTeam team, String title, String description, LocalDateTime scheduledAt) {
        return TeamSchedule.builder()
                .team(team)
                .title(title)
                .description(description)
                .scheduledAt(scheduledAt)
                .build();
    }

    public void update(String title, String description, LocalDateTime scheduledAt) {
        this.title = title;
        this.description = description;
        this.scheduledAt = scheduledAt;
    }
}
