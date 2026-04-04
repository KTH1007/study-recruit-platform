package com.study.platform.domain.team.model;

import com.study.platform.domain.user.model.User;
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
@Table(name = "team_members", uniqueConstraints = @UniqueConstraint(columnNames = {"team_id", "user_id"}))
public class TeamMember extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private StudyTeam team;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TeamMemberRole role;

    @Builder
    private TeamMember(StudyTeam team, User user, TeamMemberRole role) {
        this.team = team;
        this.user = user;
        this.role = role;
    }

    public static TeamMember createLeader(StudyTeam team, User user) {
        return TeamMember.builder()
                .team(team)
                .user(user)
                .role(TeamMemberRole.LEADER)
                .build();
    }

    public static TeamMember createMember(StudyTeam team, User user) {
        return TeamMember.builder()
                .team(team)
                .user(user)
                .role(TeamMemberRole.MEMBER)
                .build();
    }

    public void upgradeToLeader() {
        this.role = TeamMemberRole.LEADER;
    }

    public void downgradeToMember() {
        this.role = TeamMemberRole.MEMBER;
    }
}
