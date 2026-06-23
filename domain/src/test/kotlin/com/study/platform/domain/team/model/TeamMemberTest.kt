package com.study.platform.domain.team.model

import com.study.platform.domain.user.model.User
import com.study.platform.global.exception.CustomException
import com.study.platform.global.exception.ErrorCode
import org.assertj.core.api.AssertionsForClassTypes.assertThat
import org.assertj.core.api.AssertionsForClassTypes.assertThatNoException
import org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.test.util.ReflectionTestUtils
import java.util.UUID

class TeamMemberTest {

    private lateinit var team: StudyTeam
    private lateinit var user: User

    @BeforeEach
    fun setUp() {
        val author = User.create("kakao-1", "작성자", "author@test.com")
        ReflectionTestUtils.setField(author, "id", UUID.randomUUID())

        user = User.create("kakao-2", "팀원", "member@test.com")
        ReflectionTestUtils.setField(user, "id", UUID.randomUUID())

        team = StudyTeam()
        ReflectionTestUtils.setField(team, "id", UUID.randomUUID())
    }

    @Test
    fun `isLeader_LEADER역할_true`() {
        // given
        val leader = TeamMember.createLeader(team, user)

        // when & then
        assertThat(leader.isLeader()).isTrue()
    }

    @Test
    fun `isLeader_MEMBER역할_false`() {
        // given
        val member = TeamMember.createMember(team, user)

        // when & then
        assertThat(member.isLeader()).isFalse()
    }

    @Test
    fun `validateIsLeader_LEADER역할_예외없음`() {
        // given
        val leader = TeamMember.createLeader(team, user)

        // when & then
        assertThatNoException().isThrownBy { leader.validateIsLeader() }
    }

    @Test
    fun `validateIsLeader_MEMBER역할_예외발생`() {
        // given
        val member = TeamMember.createMember(team, user)

        // when & then
        assertThatThrownBy { member.validateIsLeader() }
            .isInstanceOfSatisfying(CustomException::class.java) { e ->
                assertThat(e.errorCode).isEqualTo(ErrorCode.FORBIDDEN)
            }
    }
}
