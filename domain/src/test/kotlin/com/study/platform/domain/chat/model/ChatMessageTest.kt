package com.study.platform.domain.chat.model

import com.study.platform.global.exception.CustomException
import com.study.platform.global.exception.ErrorCode
import com.study.platform.support.TestFixtures
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.assertj.core.api.AssertionsForClassTypes.assertThat
import org.junit.jupiter.api.Test

class ChatMessageTest {

    @Test
    fun `create_내용이_빈문자열이면_예외발생`() {
        val team = TestFixtures.createStudyTeam()
        val sender = TestFixtures.createUser()

        assertThatThrownBy { ChatMessage.create(team, sender, "   ") }
            .isInstanceOfSatisfying(CustomException::class.java) { e ->
                assertThat(e.errorCode).isEqualTo(ErrorCode.INVALID_INPUT)
            }
    }

    @Test
    fun `create_내용이_1000자_초과면_예외발생`() {
        val team = TestFixtures.createStudyTeam()
        val sender = TestFixtures.createUser()
        val tooLong = "a".repeat(1001)

        assertThatThrownBy { ChatMessage.create(team, sender, tooLong) }
            .isInstanceOfSatisfying(CustomException::class.java) { e ->
                assertThat(e.errorCode).isEqualTo(ErrorCode.INVALID_INPUT)
            }
    }
}
