package com.study.platform.domain.team.model;

import com.study.platform.domain.user.model.User;
import com.study.platform.global.exception.CustomException;
import com.study.platform.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.junit.jupiter.api.Assertions.*;

class TeamMemberTest {

    private StudyTeam team;
    private User user;

    @BeforeEach
    void setUp() {
        User author = User.create("kakao-1", "작성자", "author@test.com");
        ReflectionTestUtils.setField(author, "id", UUID.randomUUID());

        user = User.create("kakao-2", "팀원", "member@test.com");
        ReflectionTestUtils.setField(user, "id", UUID.randomUUID());

        // StudyTeam은 post 없이 직접 생성
        team = new StudyTeam();
        ReflectionTestUtils.setField(team, "id", UUID.randomUUID());
    }

    @Test
    void isLeader_LEADER역할_true() {
        // given
        TeamMember leader = TeamMember.createLeader(team, user);

        // when & then
        assertThat(leader.isLeader()).isTrue();
    }

    @Test
    void isLeader_MEMBER역할_false() {
        // given
        TeamMember member = TeamMember.createMember(team, user);

        // when & then
        assertThat(member.isLeader()).isFalse();
    }

    @Test
    void validateIsLeader_LEADER역할_예외없음() {
        // given
        TeamMember leader = TeamMember.createLeader(team, user);

        // when & then
        assertThatNoException().isThrownBy(leader::validateIsLeader);
    }

    @Test
    void validateIsLeader_MEMBER역할_예외발생() {
        // given
        TeamMember member = TeamMember.createMember(team, user);

        // when & then
        assertThatThrownBy(member::validateIsLeader)
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                        .isEqualTo(ErrorCode.FORBIDDEN));
    }
}