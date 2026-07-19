package com.study.platform.domain.team.model

import com.study.platform.global.exception.CustomException
import com.study.platform.global.exception.ErrorCode
import com.study.platform.support.TestFixtures
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.assertj.core.api.AssertionsForClassTypes.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class TeamScheduleTest {

    @Test
    fun `create_제목이_빈문자열이면_예외발생`() {
        val team = TestFixtures.createStudyTeam()

        assertThatThrownBy { TeamSchedule.create(team, "   ", null, LocalDateTime.now().plusDays(1)) }
            .isInstanceOfSatisfying(CustomException::class.java) { e ->
                assertThat(e.errorCode).isEqualTo(ErrorCode.INVALID_INPUT)
            }
    }

    @Test
    fun `create_제목이_100자_초과면_예외발생`() {
        val team = TestFixtures.createStudyTeam()
        val tooLong = "a".repeat(101)

        assertThatThrownBy { TeamSchedule.create(team, tooLong, null, LocalDateTime.now().plusDays(1)) }
            .isInstanceOfSatisfying(CustomException::class.java) { e ->
                assertThat(e.errorCode).isEqualTo(ErrorCode.INVALID_INPUT)
            }
    }

    @Test
    fun `create_내용이_1000자_초과면_예외발생`() {
        val team = TestFixtures.createStudyTeam()
        val tooLong = "a".repeat(1001)

        assertThatThrownBy { TeamSchedule.create(team, "일정 제목", tooLong, LocalDateTime.now().plusDays(1)) }
            .isInstanceOfSatisfying(CustomException::class.java) { e ->
                assertThat(e.errorCode).isEqualTo(ErrorCode.INVALID_INPUT)
            }
    }

    @Test
    fun `update_제목이_빈문자열이면_예외발생`() {
        val schedule = TestFixtures.createTeamSchedule()

        assertThatThrownBy { schedule.update("", null, LocalDateTime.now().plusDays(1)) }
            .isInstanceOfSatisfying(CustomException::class.java) { e ->
                assertThat(e.errorCode).isEqualTo(ErrorCode.INVALID_INPUT)
            }
    }
}
